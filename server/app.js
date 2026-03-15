const express = require('express');
const cors = require('cors');
const path = require('path');
const Database = require('better-sqlite3');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const nodemailer = require('nodemailer');

// ─── App Setup ────────────────────────────────────────────────
const app = express();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'agricoop_super_secret_key_2026';

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));
app.use(express.static(path.join(__dirname, '../client')));

// ─── Database Setup ───────────────────────────────────────────
const db = new Database(path.join(__dirname, 'database.sqlite'));
db.pragma('journal_mode = WAL');

db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT DEFAULT 'farmer',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS registrations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    farm_location TEXT NOT NULL,
    crop_type TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    farm_size REAL,
    user_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
  );

  CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT NOT NULL DEFAULT 'Grains',
    price REAL NOT NULL,
    unit TEXT NOT NULL DEFAULT 'kg',
    stock INTEGER DEFAULT 100,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS contact_messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    subject TEXT,
    message TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    date TEXT NOT NULL,
    location TEXT NOT NULL,
    type TEXT NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS knowledge_resources (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_email TEXT NOT NULL,
    product_name TEXT NOT NULL,
    category TEXT NOT NULL,
    price REAL NOT NULL,
    farmer_name TEXT,
    status TEXT DEFAULT 'Pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );
`);

// ─── Seed Events (if empty) ──────────────────────────────────
const eventCount = db.prepare('SELECT COUNT(*) as count FROM events').get();
if (eventCount.count === 0) {
    const insert = db.prepare('INSERT INTO events (title, date, location, type, description) VALUES (?, ?, ?, ?, ?)');
    const tx = db.transaction((items) => { for (const i of items) insert.run(...i); });
    tx([
        ['Organic Farming Workshop', '2026-03-25', 'Agri Hub, Bangalore', 'Workshop', 'Learn the basics of transition to organic farming.'],
        ['Smart Irrigation Seminar', '2026-04-05', 'Community Center, Pune', 'Seminar', 'Using IoT sensors for water conservation.'],
        ['Rural Tech Expo 2026', '2026-04-12', 'Vigan Bhavan, Delhi', 'Expo', 'Showcasing the latest in rural technology.'],
    ]);
    console.log('✅ Seeded events');
}

// ─── Seed Products ──────────────────────────────────────────
const productCount = db.prepare('SELECT COUNT(*) as count FROM products').get();
if (productCount.count === 0) {
    const insert = db.prepare('INSERT INTO products (name, category, price, unit, stock) VALUES (?, ?, ?, ?, ?)');
    const tx = db.transaction((items) => { for (const i of items) insert.run(...i); });
    tx([
        ['Premium Basmati Rice', 'Grains', 120, 'kg', 500],
        ['Organic Whole Wheat', 'Grains', 45, 'kg', 1000],
        ['Pure Mustard Oil', 'Oils', 180, 'Litre', 200],
        ['Fresh Turmeric Powder', 'Spices', 250, 'kg', 150],
        ['Natural Forest Honey', 'Sweeteners', 450, 'kg', 80],
        ['Roasted Groundnuts', 'Snacks', 140, 'kg', 300],
        ['A2 Desi Cow Ghee', 'Dairy', 1200, 'kg', 40],
        ['Organic Jaggery Blocks', 'Sweeteners', 90, 'kg', 250],
    ]);
    console.log('✅ Seeded products');
}

// ─── Auth Middleware ──────────────────────────────────────────
function authMiddleware(req, res, next) {
    const header = req.headers.authorization;
    if (!header || !header.startsWith('Bearer ')) {
        return res.status(401).json({ success: false, message: 'Not authenticated' });
    }
    try {
        const token = header.split(' ')[1];
        req.user = jwt.verify(token, JWT_SECRET);
        next();
    } catch {
        return res.status(401).json({ success: false, message: 'Invalid or expired token' });
    }
}

// ═══════════════════════════════════════════════════════════════
//  AUTH ROUTES
// ═══════════════════════════════════════════════════════════════

// POST /api/auth/signup
app.post('/api/auth/signup', (req, res) => {
    try {
        const { full_name, email, password } = req.body;
        if (!full_name || !email || !password) {
            return res.status(400).json({ success: false, message: 'All fields are required.' });
        }
        if (password.length < 6) {
            return res.status(400).json({ success: false, message: 'Password must be at least 6 characters.' });
        }

        const existing = db.prepare('SELECT id FROM users WHERE email = ?').get(email);
        if (existing) {
            return res.status(409).json({ success: false, message: 'Email already registered.' });
        }

        const hash = bcrypt.hashSync(password, 10);
        const result = db.prepare('INSERT INTO users (full_name, email, password) VALUES (?, ?, ?)').run(full_name, email, hash);

        const token = jwt.sign({ id: result.lastInsertRowid, email, full_name, role: 'farmer' }, JWT_SECRET, { expiresIn: '7d' });

        res.status(201).json({
            success: true,
            message: 'Account created!',
            token,
            user: { id: result.lastInsertRowid, full_name, email, role: 'farmer' },
        });
    } catch (err) {
        console.error('Signup error:', err);
        res.status(500).json({ success: false, message: 'Signup failed' });
    }
});

// POST /api/auth/login
app.post('/api/auth/login', (req, res) => {
    try {
        const { email, password } = req.body;
        if (!email || !password) {
            return res.status(400).json({ success: false, message: 'Email and password are required.' });
        }

        const user = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
        if (!user || !bcrypt.compareSync(password, user.password)) {
            return res.status(401).json({ success: false, message: 'Invalid email or password.' });
        }

        const token = jwt.sign({ id: user.id, email: user.email, full_name: user.full_name, role: user.role }, JWT_SECRET, { expiresIn: '7d' });

        res.json({
            success: true,
            message: 'Login successful!',
            token,
            user: { id: user.id, full_name: user.full_name, email: user.email, role: user.role },
        });
    } catch (err) {
        console.error('Login error:', err);
        res.status(500).json({ success: false, message: 'Login failed' });
    }
});

// ═══════════════════════════════════════════════════════════════
//  EVENTS ROUTES
// ═══════════════════════════════════════════════════════════════

app.get('/api/events', (req, res) => {
    try {
        const events = db.prepare('SELECT * FROM events ORDER BY date ASC').all();
        res.json({ success: true, data: events });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Failed to fetch events' });
    }
});

// ═══════════════════════════════════════════════════════════════
//  PLANT DOCTOR AI (MOCK)
// ═══════════════════════════════════════════════════════════════

app.post('/api/plant-doctor', (req, res) => {
    try {
        const { image } = req.body;
        if (!image) return res.status(400).json({ success: false, message: 'Image is required' });

        // Mock AI Quality Verification
        const results = [
            { disease: 'A+ Grade Basmati', confidence: 98, treatment: 'Verification Success: Grain length > 8.4mm, 0% broken, aged 2 years. 100% Organic certified.' },
            { disease: 'Fresh Organic Tomatoes', confidence: 96, treatment: 'Verification Success: Vitamin C content optimal. No pesticide residues detected. 2 days from harvest.' },
            { disease: 'Premium Mustard Oil', confidence: 94, treatment: 'Verification Success: Cold-pressed, zero adulteration. High pungency levels confirmed.' },
            { disease: 'Wild Forest Honey', confidence: 97, treatment: 'Verification Success: No added sugar/C4 syrup. Pollen count indicates authentic multi-flora source.' }
        ];

        // Pick a random result for mock
        const result = results[Math.floor(Math.random() * results.length)];

        setTimeout(() => {
            res.json({ success: true, result });
        }, 2500); // 2.5s simulated scan

    } catch (err) {
        res.status(500).json({ success: false, message: 'Quality verification failed' });
    }
});

// GET /api/auth/me — Get current user profile (protected)
app.get('/api/auth/me', authMiddleware, (req, res) => {
    const user = db.prepare('SELECT id, full_name, email, role, created_at FROM users WHERE id = ?').get(req.user.id);
    if (!user) return res.status(404).json({ success: false, message: 'User not found' });
    res.json({ success: true, user });
});

// ═══════════════════════════════════════════════════════════════
//  EXISTING API ROUTES
// ═══════════════════════════════════════════════════════════════

app.post('/api/login-notify', async (req, res) => {
    try {
        const { email } = req.body;
        
        // 1. SETUP THE SENDER (Replace with your actual Gmail & App Password)
        const transporter = nodemailer.createTransport({
            service: 'gmail',
            auth: {
                user: 'YOUR_ACTUAL_GMAIL@gmail.com',         // <-- PUT YOUR EMAIL HERE
                pass: 'YOUR_16_CHARACTER_APP_PASSWORD'       // <-- PUT YOUR APP PASSWORD HERE
            }
        });

        // 2. DEFINE THE EMAIL
        const mailOptions = {
            from: '"AgriCoop Alerts" <YOUR_ACTUAL_GMAIL@gmail.com>', // Sender
            to: email,                                               // Send it to the user who logged in
            // to: 'YOUR_ACTUAL_GMAIL@gmail.com',                    // (Uncomment this line to send it to YOURSELF instead)
            subject: 'AgriCoop - Login Alert',
            text: `Hello, a login was just detected on the AgriCoop app for your account: ${email}.`
        };

        // 3. SEND IT
        await transporter.sendMail(mailOptions);
        console.log(`✅ Login email sent to ${email}`);
        res.json({ success: true, message: 'Email sent successfully!' });
    } catch (err) {
        console.error('❌ Error sending email:', err.message);
        res.status(500).json({ success: false, message: 'Failed to send email.' });
    }
});

app.get('/api/products', (req, res) => {
    try {
        const products = db.prepare('SELECT * FROM products ORDER BY name').all();
        res.json({ success: true, data: products });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Failed to fetch products' });
    }
});

app.put('/api/products/:id', authMiddleware, (req, res) => {
    try {
        const { price, stock } = req.body;
        if (price == null) return res.status(400).json({ success: false, message: 'Price is required' });
        const result = db.prepare('UPDATE products SET price = ?, stock = ? WHERE id = ?').run(price, stock ?? 100, req.params.id);
        if (result.changes === 0) return res.status(404).json({ success: false, message: 'Not found' });
        res.json({ success: true, message: 'Product updated' });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Failed to update product' });
    }
});

app.post('/api/orders', (req, res) => {
    try {
        const { user_email, product_name, category, price, farmer_name } = req.body;
        if (!user_email || !product_name || !price) {
            return res.status(400).json({ success: false, message: 'Incomplete order data.' });
        }
        db.prepare('INSERT INTO orders (user_email, product_name, category, price, farmer_name) VALUES (?, ?, ?, ?, ?)').run(user_email, product_name, category, price, farmer_name);
        res.status(201).json({ success: true, message: 'Order placed successfully!' });
    } catch (err) {
        console.error('Order error:', err);
        res.status(500).json({ success: false, message: 'Failed to place order.' });
    }
});

app.get('/api/orders/:email', (req, res) => {
    try {
        const orders = db.prepare('SELECT * FROM orders WHERE user_email = ? ORDER BY created_at DESC').all(req.params.email);
        res.json({ success: true, data: orders });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Failed to fetch orders.' });
    }
});

app.post('/api/register', (req, res) => {
    try {
        const { first_name, last_name, farm_location, crop_type, email, phone, farm_size } = req.body;
        if (!first_name || !last_name || !farm_location || !crop_type) {
            return res.status(400).json({ success: false, message: 'All fields required.' });
        }
        const result = db.prepare('INSERT INTO registrations (first_name, last_name, farm_location, crop_type, email, phone, farm_size) VALUES (?, ?, ?, ?, ?, ?, ?)').run(first_name, last_name, farm_location, crop_type, email || null, phone || null, farm_size || null);
        res.status(201).json({ success: true, message: 'Farm registered successfully!', id: result.lastInsertRowid });
    } catch (err) {
        console.error('Registration error:', err);
        res.status(500).json({ success: false, message: 'Registration failed' });
    }
});

app.get('/api/registrations', authMiddleware, (req, res) => {
    try {
        const data = db.prepare('SELECT * FROM registrations ORDER BY created_at DESC').all();
        res.json({ success: true, data });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Failed to fetch registrations' });
    }
});

app.post('/api/contact', (req, res) => {
    try {
        const { name, email, subject, message } = req.body;
        if (!name || !email || !message) return res.status(400).json({ success: false, message: 'All fields required.' });
        const result = db.prepare('INSERT INTO contact_messages (name, email, subject, message) VALUES (?, ?, ?, ?)').run(name, email, subject || null, message);
        res.status(201).json({ success: true, message: 'Message sent!', id: result.lastInsertRowid });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Failed to send message' });
    }
});

app.get('/api/contact', authMiddleware, (req, res) => {
    try {
        const data = db.prepare('SELECT * FROM contact_messages ORDER BY created_at DESC').all();
        res.json({ success: true, data });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Failed to fetch messages' });
    }
});

app.get('/api/stats', authMiddleware, (req, res) => {
    try {
        const total_farms = db.prepare('SELECT COUNT(*) as c FROM registrations').get().c;
        const total_messages = db.prepare('SELECT COUNT(*) as c FROM contact_messages').get().c;
        const total_products = db.prepare('SELECT COUNT(*) as c FROM products').get().c;
        const total_users = db.prepare('SELECT COUNT(*) as c FROM users').get().c;
        const crop_breakdown = db.prepare('SELECT crop_type, COUNT(*) as count FROM registrations GROUP BY crop_type ORDER BY count DESC').all();
        res.json({ success: true, data: { total_farms, total_messages, total_products, total_users, crop_breakdown } });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Failed to fetch stats' });
    }
});

app.get('{*path}', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html'));
});

app.listen(PORT, () => {
    console.log(`\n  🌾 AgriCoop Server Running → http://localhost:${PORT}\n`);
});

process.on('SIGINT', () => { db.close(); process.exit(0); });

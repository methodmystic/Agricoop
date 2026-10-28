// ========================================================
// AgriCoop - Main Application JavaScript
// index.js - JDBC & MySQL REST Backend Integration
// (Replaced Firebase with pure Java JDBC Backend)
// ========================================================

const API_BASE = window.location.port === '3000' || window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
    ? 'http://localhost:3000/api'
    : '/api';

// ========================================
// DOM Element References
// ========================================
let isLoginMode = true;
const authBtn = document.getElementById('auth-btn');
const userProfile = document.getElementById('user-profile');
const userEmailSpan = document.getElementById('user-email');
const logoutBtn = document.getElementById('logout-btn');
const authModal = document.getElementById('auth-modal');
const authForm = document.getElementById('auth-form');
const authNameGroup = document.getElementById('auth-name-group');
const authName = document.getElementById('auth-name');
const authEmail = document.getElementById('auth-email');
const authPassword = document.getElementById('auth-password');
const authToggleBtn = document.getElementById('auth-toggle-btn');
const modalTitle = document.getElementById('modal-title');
const modalDesc = document.getElementById('modal-desc');
const authSubmitBtn = document.getElementById('auth-submit-btn');
const authToggleText = document.getElementById('auth-toggle-text');
const authError = document.getElementById('auth-error');

// ========================================
// Auth State Helpers
// ========================================
function checkAuthState() {
    const savedEmail = localStorage.getItem('userEmail');
    if (savedEmail) {
        if (authBtn) authBtn.classList.add('hidden');
        if (userProfile) {
            userProfile.classList.remove('hidden');
            userProfile.classList.add('flex');
        }
        if (userEmailSpan) userEmailSpan.textContent = savedEmail;
    } else {
        if (authBtn) authBtn.classList.remove('hidden');
        if (userProfile) {
            userProfile.classList.add('hidden');
            userProfile.classList.remove('flex');
        }
        if (userEmailSpan) userEmailSpan.textContent = '';
    }
}

// Check initial state immediately
checkAuthState();

// ========================================
// Auth Mode Toggle (Login / Register)
// ========================================
if (authToggleBtn) {
    authToggleBtn.addEventListener('click', () => {
        isLoginMode = !isLoginMode;
        if (authError) authError.classList.add('hidden');
        
        if (isLoginMode) {
            modalTitle.textContent = 'Welcome Back';
            modalDesc.textContent = 'Sign in to your AgriCoop account';
            authSubmitBtn.textContent = 'Sign In';
            authToggleText.textContent = "Don't have an account?";
            authToggleBtn.textContent = 'Register now';
            if (authNameGroup) authNameGroup.classList.add('hidden');
            if (authName) authName.removeAttribute('required');
        } else {
            modalTitle.textContent = 'Create Account';
            modalDesc.textContent = 'Join the AgriCoop MySQL network';
            authSubmitBtn.textContent = 'Register';
            authToggleText.textContent = "Already have an account?";
            authToggleBtn.textContent = 'Sign in';
            if (authNameGroup) authNameGroup.classList.remove('hidden');
            if (authName) authName.setAttribute('required', 'true');
        }
    });
}

// Open modal button
if (authBtn) {
    authBtn.addEventListener('click', () => {
        if (authModal) authModal.classList.remove('hidden');
        if (authError) authError.classList.add('hidden');
    });
}

// ========================================
// Auth Form Submission (Pure JDBC MySQL API)
// ========================================
if (authForm) {
    authForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = authEmail.value.trim();
        const password = authPassword.value;
        const fullName = authName && authName.value ? authName.value.trim() : (email.split('@')[0] || 'Farmer');

        if (authError) authError.classList.add('hidden');
        authSubmitBtn.disabled = true;
        authSubmitBtn.style.opacity = '0.7';

        try {
            // Helper: safely parse JSON response (avoids crash when server returns HTML error page)
            async function safeJson(res) {
                const text = await res.text();
                try { return JSON.parse(text); }
                catch (_) { return null; }
            }

            if (isLoginMode) {
                // --- LOGIN VIA JDBC MYSQL ---
                let response, result;
                try {
                    response = await fetch(`${API_BASE}/auth/login`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ email, password })
                    });
                    result = await safeJson(response);
                } catch (_) {
                    result = null;
                }

                if (!result) {
                    // Server offline — try localStorage credentials
                    const stored = JSON.parse(localStorage.getItem('currentUser') || 'null');
                    if (stored && stored.email === email) {
                        checkAuthState();
                        if (authModal) authModal.classList.add('hidden');
                        authForm.reset();
                        alert(`Welcome back, ${stored.full_name || email}! (Offline mode — MySQL server not reachable)`);
                        return;
                    }
                    throw new Error('Cannot reach AgriCoop server. Start the Java backend to sign in.');
                }

                if (!response.ok || !result.success) {
                    throw new Error(result.message || 'Login failed. Please verify your credentials.');
                }

                // Store verified session
                localStorage.setItem('currentUser', JSON.stringify(result.user || { email, full_name: fullName }));
                localStorage.setItem('userEmail', email);
                checkAuthState();

                // Optional Login Notification Alert (fire-and-forget)
                fetch(`${API_BASE}/login-notify`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email })
                }).catch(() => {});

                if (authModal) authModal.classList.add('hidden');
                authForm.reset();
                alert(`Welcome back, ${result.user?.full_name || email}! Signed in via MySQL.`);

            } else {
                // --- SIGNUP / REGISTER VIA JDBC MYSQL ---
                let response, result;
                try {
                    response = await fetch(`${API_BASE}/auth/signup`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ full_name: fullName, email, password })
                    });
                    result = await safeJson(response);
                } catch (_) {
                    result = null;
                }

                if (!result) {
                    // Server offline — save account locally so user isn't stuck
                    const offlineUser = { full_name: fullName, email, created_at: new Date().toISOString(), offline: true };
                    localStorage.setItem('currentUser', JSON.stringify(offlineUser));
                    localStorage.setItem('userEmail', email);
                    checkAuthState();
                    if (authModal) authModal.classList.add('hidden');
                    authForm.reset();
                    alert(`Account saved locally, ${fullName}! Start the Java backend to sync to MySQL.`);
                    return;
                }

                if (!response.ok || !result.success) {
                    throw new Error(result.message || 'Registration failed. Email may already be in use.');
                }

                // Auto-login newly registered user
                localStorage.setItem('currentUser', JSON.stringify(result.user || { email, full_name: fullName }));
                localStorage.setItem('userEmail', email);
                checkAuthState();

                if (authModal) authModal.classList.add('hidden');
                authForm.reset();
                alert(`Account created in AgriCoop MySQL database! Welcome, ${fullName}.`);
            }
        } catch (error) {
            console.error('Authentication Error:', error);
            const msg = error.message && !error.message.includes('JSON')
                ? error.message
                : 'Server offline. Start the Java backend to use MySQL auth.';
            if (authError) {
                authError.textContent = msg;
                authError.classList.remove('hidden');
            } else {
                alert(msg);
            }
        } finally {
            authSubmitBtn.disabled = false;
            authSubmitBtn.style.opacity = '1';
        }
    });
}

// ========================================
// Logout Handler
// ========================================
if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
        localStorage.removeItem('userEmail');
        localStorage.removeItem('currentUser');
        checkAuthState();
    });
}

// ========================================
// Farm Registration Form Handler (JDBC MySQL)
// ========================================
const regForm = document.getElementById('register-form');
const regMessage = document.getElementById('reg-message');

if (regForm) {
    regForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('reg-submit-btn');
        btn.disabled = true;
        btn.innerText = 'Submitting to AgriCoop DB...';

        const rawSize = document.getElementById('reg-size')?.value;
        const data = {
            first_name: document.getElementById('reg-first-name').value.trim(),
            last_name: document.getElementById('reg-last-name').value.trim(),
            farm_location: document.getElementById('reg-location').value.trim(),
            crop_type: document.getElementById('reg-crop').value,
            email: document.getElementById('reg-email')?.value?.trim() || null,
            farm_size: rawSize ? parseFloat(rawSize) : null
        };

        try {
            console.log("Submitting farm registration to Java JDBC backend:", data);

            const response = await fetch(`${API_BASE}/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            const result = await response.json();
            if (!response.ok || !result.success) {
                throw new Error(result.message || 'Server error while saving registration.');
            }

            regMessage.classList.remove('hidden', 'text-red-500', 'bg-red-50');
            regMessage.classList.add('text-green-700', 'p-3', 'bg-green-50', 'mb-4', 'border', 'border-green-200');
            regMessage.innerText = `Farm registered successfully in MySQL! (Record ID: #${result.id || 'Saved'})`;
            alert('Farm registration successfully stored in MySQL database via JDBC!');
            regForm.reset();

        } catch (err) {
            console.warn("Backend error or offline fallback:", err);
            // Backup to local storage if offline
            const offlineRegistrations = JSON.parse(localStorage.getItem('agriRegistrations') || '[]');
            offlineRegistrations.push({ ...data, created_at: new Date().toISOString() });
            localStorage.setItem('agriRegistrations', JSON.stringify(offlineRegistrations));

            regMessage.classList.remove('hidden', 'text-red-500', 'bg-red-50');
            regMessage.classList.add('text-green-700', 'p-3', 'bg-green-50', 'mb-4', 'border', 'border-green-200');
            regMessage.innerText = 'Registration saved locally! Will sync when Java MySQL server is connected.';
            alert('Registration recorded! Note: Connect to Java backend for live MySQL storage.');
            regForm.reset();
        } finally {
            btn.disabled = false;
            btn.innerText = 'Submit Registration';
        }
    });
}

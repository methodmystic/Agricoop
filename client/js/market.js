// ========================================
// AgriCoop - Marketplace Page JavaScript
// market.js - Product filtering, orders, deals
// ========================================

// ========================================
// Configuration
// ========================================
const API_BASE = 'http://localhost:3000/api';

// Mock user email for now (normally from login state)
const currentUserEmail = localStorage.getItem('userEmail') || 'customer@example.com';

// ========================================
// View Navigation
// ========================================
function showView(viewName) {
    // Hide all views
    document.getElementById('view-marketplace').classList.add('hidden');
    document.getElementById('view-details').classList.add('hidden');
    document.getElementById('view-orders').classList.add('hidden');
    
    // Show target view
    document.getElementById('view-' + viewName).classList.remove('hidden');
    
    // Update sidebar UI
    document.querySelectorAll('.nav-item').forEach(el => {
        el.classList.remove('bg-[#e8f3f0]', 'text-primary', 'font-semibold');
        el.classList.add('text-slate-500', 'font-medium');
    });
    
    const activeNav = document.getElementById('nav-' + (viewName === 'marketplace' ? 'market' : viewName));
    if (activeNav) {
        activeNav.classList.add('bg-[#e8f3f0]', 'text-primary', 'font-semibold');
        activeNav.classList.remove('text-slate-500', 'font-medium');
    }

    if (viewName === 'orders') {
        loadOrders();
    }
    
    document.getElementById('main-scroll').scrollTop = 0;
}

// ========================================
// Product Details View
// ========================================
function showProductDetails(name, cat, price, img) {
    document.getElementById('detail-title').innerText = name + " (Premium)";
    document.getElementById('detail-img').src = img;
    
    document.getElementById('view-marketplace').classList.add('hidden');
    document.getElementById('view-details').classList.remove('hidden');
    document.getElementById('main-scroll').scrollTop = 0;
}

function hideProductDetails() {
    document.getElementById('view-details').classList.add('hidden');
    document.getElementById('view-marketplace').classList.remove('hidden');
    document.getElementById('main-scroll').scrollTop = 0;
}

// ========================================
// Product Category Filtering
// ========================================
function filterProducts(category) {
    const cards = document.querySelectorAll('.product-card');
    const btns = document.querySelectorAll('.category-btn');

    // Update buttons
    btns.forEach(btn => {
        if (btn.innerText === category) {
            btn.classList.add('bg-primary', 'text-white');
            btn.classList.remove('bg-white', 'text-slate-600');
        } else {
            btn.classList.remove('bg-primary', 'text-white');
            btn.classList.add('bg-white', 'text-slate-600');
        }
    });

    // Show/Hide cards
    cards.forEach(card => {
        if (category === 'All' || card.getAttribute('data-category') === category) {
            card.classList.remove('hidden');
        } else {
            card.classList.add('hidden');
        }
    });
}

// ========================================
// Order Placement
// ========================================
async function placeOrder(productName, category, price, farmerName) {
    try {
        const order = {
            id: Date.now(),
            user_email: currentUserEmail,
            product_name: productName,
            category: category,
            price: price,
            farmer_name: farmerName,
            status: 'Processing',
            created_at: new Date().toISOString()
        };
        const existing = JSON.parse(localStorage.getItem('agriOrders') || '[]');
        existing.push(order);
        localStorage.setItem('agriOrders', JSON.stringify(existing));
        alert('Order placed successfully! Check "My Orders" tab.');
    } catch (err) {
        console.error('Order error:', err);
        alert('Error placing order.');
    }
}

// ========================================
// Load Orders List
// ========================================
async function loadOrders() {
    const list = document.getElementById('orders-list');
    list.innerHTML = '<tr><td colspan="5" class="px-8 py-10 text-center text-slate-400">Loading your orders...</td></tr>';

    try {
        const orders = JSON.parse(localStorage.getItem('agriOrders') || '[]');
        const userOrders = orders.filter(o => o.user_email === currentUserEmail);
        
        if (userOrders.length > 0) {
            list.innerHTML = userOrders.map(order => `
                <tr class="hover:bg-slate-50 transition-colors">
                    <td class="px-8 py-5">
                        <p class="font-bold text-slate-800">${order.product_name}</p>
                        <p class="text-xs text-slate-400">${order.category}</p>
                    </td>
                    <td class="px-8 py-5 text-slate-600">${order.farmer_name || 'N/A'}</td>
                    <td class="px-8 py-5 font-black text-dark">₹${order.price}</td>
                    <td class="px-8 py-5">
                        <span class="px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider ${
                            order.status === 'Delivered' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'
                        }">${order.status}</span>
                    </td>
                    <td class="px-8 py-5 text-slate-400 text-xs">${new Date(order.created_at).toLocaleDateString()}</td>
                </tr>
            `).join('');
        } else {
            list.innerHTML = '<tr><td colspan="5" class="px-8 py-10 text-center text-slate-400">No orders found. Buy some fresh produce!</td></tr>';
        }
    } catch (err) {
        list.innerHTML = '<tr><td colspan="5" class="px-8 py-10 text-center text-red-400">Error loading orders.</td></tr>';
    }
}

// ========================================
// Expose functions globally for onclick handlers
// ========================================
window.showView = showView;
window.showProductDetails = showProductDetails;
window.hideProductDetails = hideProductDetails;
window.filterProducts = filterProducts;
window.placeOrder = placeOrder;
window.loadOrders = loadOrders;

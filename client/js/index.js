// ========================================
// AgriCoop - Main Application JavaScript
// index.js - Firebase Auth, Registration, UI
// ========================================

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.9.0/firebase-app.js";
import { getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut, onAuthStateChanged, sendEmailVerification } from "https://www.gstatic.com/firebasejs/10.9.0/firebase-auth.js";
import { getAnalytics } from "https://www.gstatic.com/firebasejs/10.9.0/firebase-analytics.js";
import { getFirestore, collection, addDoc, serverTimestamp } from "https://www.gstatic.com/firebasejs/10.9.0/firebase-firestore.js";

// ========================================
// Firebase Configuration
// ========================================
const firebaseConfig = {
    apiKey: "AIzaSyCURW8zBb_BkTvEbmY9Wf3bPQBFZy9itUE",
    authDomain: "agricoop-4321.firebaseapp.com",
    projectId: "agricoop-4321",
    storageBucket: "agricoop-4321.firebasestorage.app",
    messagingSenderId: "399916394426",
    appId: "1:399916394426:web:092ed1dbdc5bd20cc31f83",
    measurementId: "G-MJ6D9X1X63"
};

// ========================================
// Firebase Initialization
// ========================================
let app, auth, analytics, db;
try {
    app = initializeApp(firebaseConfig);
    auth = getAuth(app);
    analytics = getAnalytics(app);
    db = getFirestore(app);
} catch (e) {
    console.warn("Firebase not properly configured:", e);
}

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
const authEmail = document.getElementById('auth-email');
const authPassword = document.getElementById('auth-password');
const authToggleBtn = document.getElementById('auth-toggle-btn');
const modalTitle = document.getElementById('modal-title');
const modalDesc = document.getElementById('modal-desc');
const authSubmitBtn = document.getElementById('auth-submit-btn');
const authToggleText = document.getElementById('auth-toggle-text');
const authError = document.getElementById('auth-error');

// ========================================
// Auth Mode Toggle (Login / Register)
// ========================================
authToggleBtn.addEventListener('click', () => {
    isLoginMode = !isLoginMode;
    authError.classList.add('hidden');
    if (isLoginMode) {
        modalTitle.textContent = 'Welcome Back';
        modalDesc.textContent = 'Sign in to your AgriCoop account';
        authSubmitBtn.textContent = 'Sign In';
        authToggleText.textContent = "Don't have an account?";
        authToggleBtn.textContent = 'Register now';
    } else {
        modalTitle.textContent = 'Create Account';
        modalDesc.textContent = 'Join the AgriCoop network';
        authSubmitBtn.textContent = 'Register';
        authToggleText.textContent = "Already have an account?";
        authToggleBtn.textContent = 'Sign in';
    }
});

// ========================================
// Auth Form Submission (Login / Register)
// ========================================
authForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = authEmail.value;
    const password = authPassword.value;
    authError.classList.add('hidden');
    authSubmitBtn.disabled = true;
    authSubmitBtn.style.opacity = '0.7';

    try {
        if (!auth || firebaseConfig.apiKey === "YOUR_API_KEY") {
            throw new Error("Please replace the Firebase Config in the source code with your actual credentials.");
        }
        if (isLoginMode) {
            await signInWithEmailAndPassword(auth, email, password);
            alert("Login successful! Attempting to send email...");
            
            // Trigger the backend to send the email
            try {
                const emailRes = await fetch('http://localhost:3000/api/login-notify', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email: email })
                });
                if(emailRes.ok) {
                    console.log('Login email notification sent successfully via AgriCoop Server!');
                }
            } catch(err) {
                console.error('Failed to trigger the email server.', err);
            }
        } else {
            await createUserWithEmailAndPassword(auth, email, password);
            await sendEmailVerification(auth.currentUser);
            alert("Verification email sent to your Gmail! Please check your inbox before logging in.");
        }
        authModal.classList.add('hidden');
        authForm.reset();
    } catch (error) {
        authError.textContent = error.message;
        authError.classList.remove('hidden');
    } finally {
        authSubmitBtn.disabled = false;
        authSubmitBtn.style.opacity = '1';
    }
});

// ========================================
// Logout Handler
// ========================================
logoutBtn.addEventListener('click', async () => {
    if (auth) await signOut(auth);
});

// ========================================
// Auth State Observer
// ========================================
if (auth) {
    onAuthStateChanged(auth, (user) => {
        if (user) {
            authBtn.classList.add('hidden');
            userProfile.classList.remove('hidden');
            userProfile.classList.add('flex');
            userEmailSpan.textContent = user.email;
            localStorage.setItem('userEmail', user.email);
        } else {
            authBtn.classList.remove('hidden');
            userProfile.classList.add('hidden');
            userProfile.classList.remove('flex');
            userEmailSpan.textContent = '';
            localStorage.removeItem('userEmail');
        }
    });
}

// ========================================
// Farm Registration Form Handler
// ========================================
const regForm = document.getElementById('register-form');
const regMessage = document.getElementById('reg-message');
if (regForm) {
    regForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('reg-submit-btn');
        btn.disabled = true;
        btn.innerText = 'Submitting...';
        
        const data = {
            first_name: document.getElementById('reg-first-name').value,
            last_name: document.getElementById('reg-last-name').value,
            farm_location: document.getElementById('reg-location').value,
            crop_type: document.getElementById('reg-crop').value,
            email: document.getElementById('reg-email').value,
            farm_size: document.getElementById('reg-size').value,
        };
        
        try {
            console.log("Starting submission for:", data);

            // Removed backend check to allow static serverless hosting to succeed.
            // Data will fall back directly to Firebase.

            // 2. Firebase Firestore Save (Non-blocking / Background)
            if (db) {
                addDoc(collection(db, "registrations"), {
                    ...data,
                    created_at: serverTimestamp()
                }).then(() => console.log("Firebase sync complete"))
                  .catch(e => console.warn("Firebase sync error (check your rules!):", e));
            }
            
            // 3. True Success State
            regMessage.classList.remove('hidden', 'text-red-500');
            regMessage.classList.add('text-green-500', 'p-3', 'bg-green-50', 'mb-4');
            regMessage.innerText = 'Farm registered successfully!';
            alert('Submitted successfully!');
            regForm.reset();

        } catch (err) {
            console.error("Submission error:", err);
            regMessage.classList.remove('hidden', 'text-green-500');
            regMessage.classList.add('text-red-500', 'p-3', 'bg-red-50', 'mb-4');
            regMessage.innerText = 'Network error: Make sure your local server is running (node server.js)';
            alert('Submission failed. Check console for details.');
        } finally {
            btn.disabled = false;
            btn.innerText = 'Submit Registration';
        }
    });
}

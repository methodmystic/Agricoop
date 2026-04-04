// ========================================
// AgriCoop - Analytics Page JavaScript
// analytics.js - Language switching & i18n
// ========================================

// ========================================
// Translation Data
// ========================================
const translations = {
    en: {
        back_home: "← Back to Home",
        ana_badge: "Predictive Intelligence Engine",
        ana_title: "The Data Advantage",
        ana_desc: "Transforming raw agricultural metrics into actionable financial outcomes. Precision farming, powered by the AgriCoop stack.",
        soil_title: "Soil Hydration Metrics",
        soil_desc: "Sub-surface IoT arrays transmitting moisture potential every 5 minutes. Optimized for water efficiency.",
        status_opt: "Optimal Threshold Reached",
        market_trend_title: "Market Arbitrage",
        market_trend_desc: "AI forecasting of commodity fluctuation using global trade sentiment.",
        crop_wheat: "Basmati Wheat",
        crop_rice: "Sharbati Rice",
        supply_title: "Real-Time Transit Matrix",
        supply_desc: "Every SKU is tracked via blockchain and active GPS. 0% loss, 100% accountability.",
        status_transit: "In Transit",
        expert_title: "Human Intelligence, Augmented.",
        expert_desc: "Our agronomists use the same data you see to provide hyper-local advice on crop healing and soil recovery.",
        btn_talk: "Initialize Consultation",
        stat_support: "Uptime Support",
        stat_experts: "Data Scientists"
    },
    hi: {
        back_home: "← मुख्य पृष्ठ",
        ana_badge: "पूर्वानुमान खुफिया इंजन",
        ana_title: "डेटा का लाभ",
        ana_desc: "कच्चे कृषि मेट्रिक्स को कार्रवाई योग्य वित्तीय परिणामों में बदलना। एग्रीकूप स्टैक द्वारा संचालित सटीक खेती।",
        soil_title: "मृदा हाइड्रेशन मेट्रिक्स",
        soil_desc: "उप-सतह IoT सरणियाँ हर 5 मिनट में नमी की क्षमता प्रसारित करती हैं। जल दक्षता के लिए अनुकूलित।",
        status_opt: "इष्टतम सीमा तक पहुँच गया",
        market_trend_title: "बाजार आर्बिट्रेज",
        market_trend_desc: "वैश्विक व्यापार भावना का उपयोग करके कमोडिटी उतार-चढ़ाव का AI पूर्वानुमान।",
        crop_wheat: "बासमती गेहूं",
        crop_rice: "शरबती चावल",
        supply_title: "रियल-टाइम ट्रांजिट मैट्रिक्स",
        supply_desc: "हर SKU को ब्लॉकचेन और सक्रिय GPS के माध्यम से ट्रैक किया जाता है। 0% नुकसान, 100% जवाबदेही।",
        status_transit: "पारगमन में",
        expert_title: "मानव बुद्धि, संवर्धित।",
        expert_desc: "हमारे कृषि विज्ञानी उसी डेटा का उपयोग करते हैं जिसे आप फसल उपचार और मिट्टी की रिकवरी पर सलाह देने के लिए देखते हैं।",
        btn_talk: "परामर्श शुरू करें",
        stat_support: "अपटाइम सपोर्ट",
        stat_experts: "डेटा वैज्ञानिक"
    }
};

// ========================================
// Language Switching
// ========================================
function switchLanguage() {
    const current = localStorage.getItem('agricoop_lang') || 'en';
    const next = current === 'en' ? 'hi' : 'en';

    document.body.style.opacity = '0';
    setTimeout(() => {
        applyTranslations(next);
        document.documentElement.lang = next;
        document.getElementById('lang-btn-text').textContent = next === 'en' ? 'हिन्दी' : 'English';
        localStorage.setItem('agricoop_lang', next);
        document.body.style.opacity = '1';
    }, 300);
}

// ========================================
// Apply Translations Helper
// ========================================
function applyTranslations(lang) {
    const elements = document.querySelectorAll('[data-i18n]');
    elements.forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (translations[lang] && translations[lang][key]) {
            el.innerHTML = translations[lang][key];
        }
    });
}

// ========================================
// Initialize on DOM Ready
// ========================================
document.addEventListener('DOMContentLoaded', () => {
    const savedLang = localStorage.getItem('agricoop_lang') || 'en';
    applyTranslations(savedLang);
    document.getElementById('lang-btn-text').textContent = savedLang === 'en' ? 'हिन्दी' : 'English';
    document.body.style.transition = 'opacity 0.4s ease';
});

// Expose switchLanguage globally for onclick handlers
window.switchLanguage = switchLanguage;

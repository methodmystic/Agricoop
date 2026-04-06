document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('crop-image');
    const imagePreview = document.getElementById('image-preview');
    const form = document.getElementById('disease-form');
    const resultsContainer = document.getElementById('results-container');
    
    // UI Elements
    const dName = document.getElementById('disease-name');
    const dDesc = document.getElementById('disease-desc');
    const dAction = document.getElementById('treatment-action');
    const dRisk = document.getElementById('env-risk');
    const dPrev = document.getElementById('prevention-insight');
    const confScore = document.getElementById('confidence-score');
    const severityBadge = document.getElementById('severity-badge');
    const severityText = document.getElementById('severity-text');

    // Image preview logic
    fileInput.addEventListener('change', function() {
        const file = this.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                imagePreview.style.backgroundImage = `url('${e.target.result}')`;
                imagePreview.classList.remove('hidden');
            }
            reader.readAsDataURL(file);
        }
    });

    const diseaseDB = {
        wheat: {
            name: "Wheat Yellow Rust",
            severity: "High",
            desc: "Yellow/orange pustules forming parallel lines on leaves, severely impacting photosynthesis and grain filling.",
            treatment: "Apply Propiconazole or Tebuconazole fungicide immediately. Ensure consistent coverage.",
            prevention: "Switch to rust-resistant wheat varieties for the next season. Practice crop rotation and clear volunteer plants."
        },
        rice: {
            name: "Rice Blast",
            severity: "Critical",
            desc: "Diamond-shaped white/gray lesions with dark borders. Can cause neck rot leading to total yield loss.",
            treatment: "Spray Tricyclazole 75% WP. Maintain proper water levels, avoid field drainage during outbreaks.",
            prevention: "Avoid excessive nitrogen fertilizers. Plant resistant variants and burn infected stubble post-harvest."
        },
        tomato: {
            name: "Late Blight",
            severity: "Critical",
            desc: "Dark, water-soaked spots on leaves and stems. Fruits develop dark, greasy patches.",
            treatment: "Apply Copper-based fungicides immediately. Remove and destroy infected plant parts.",
            prevention: "Ensure adequate spacing for air circulation. Avoid overhead watering to keep foliage dry."
        },
        potato: {
            name: "Early Blight",
            severity: "Moderate",
            desc: "Target-board like concentric dark rings on older leaves, causing them to yellow and drop.",
            treatment: "Use Chlorothalonil or Mancozeb. Apply at 7-14 day intervals depending on disease pressure.",
            prevention: "Maintain adequate soil fertility. Ensure 3-4 year crop rotation away from solanaceous crops."
        },
        corn: {
            name: "Northern Corn Leaf Blight",
            severity: "Moderate",
            desc: "Long, elliptical, grayish-green or tan lesions on leaves. Moves from lower to upper leaves.",
            treatment: "Fungicide application (like Pyraclostrobin) is effective if applied before silking.",
            prevention: "Deep tillage to bury infected residue. Plant resistant hybrids adapted to your region."
        }
    };

    form.addEventListener('submit', (e) => {
        e.preventDefault();

        const crop = document.getElementById('crop-type').value;
        const temp = parseFloat(document.getElementById('env-temp').value);
        const humidity = parseFloat(document.getElementById('env-humidity').value);

        // Show Results container
        resultsContainer.classList.remove('hidden');
        setTimeout(() => {
            resultsContainer.classList.remove('opacity-0', 'translate-y-10');
            resultsContainer.classList.add('opacity-100', 'translate-y-0');
        }, 50);

        // Simulate API call & ML processing
        const data = diseaseDB[crop] || {
            name: "Unidentified Stress Profile",
            severity: "Low",
            desc: "Symptoms do not strongly match a known severe pathogen in our database.",
            treatment: "Observe closely for 48 hours. Ensure optimal watering and apply a broad-spectrum organic booster.",
            prevention: "Maintain regular scouting schedule and record environmental shifts."
        };

        // Environment Risk Logic
        let riskString = "";
        if (humidity > 80 && temp > 25) {
            riskString = `High humidity (${humidity}%) & warm temp accelerates fungal growth. Immediate action required.`;
        } else if (temp < 15) {
            riskString = `Low temp (${temp}°C) makes plants vulnerable. Focus on soil warmth.`;
        } else {
            riskString = `Current environment (${temp}°C, ${humidity}%) is moderate but could shift. Monitor closely.`;
        }

        // Setup UI
        dName.textContent = data.name;
        dDesc.textContent = data.desc;
        dAction.textContent = data.treatment;
        dRisk.textContent = riskString;
        dPrev.textContent = data.prevention;
        
        confScore.textContent = (Math.random() * (98 - 85) + 85).toFixed(1) + "%";

        // Style Badge
        severityText.textContent = data.severity;
        severityBadge.className = 'px-5 py-2 rounded-full border'; // Reset
        if (data.severity === 'Critical') {
            severityBadge.classList.add('bg-red-100', 'border-red-200', 'text-red-700');
        } else if (data.severity === 'High') {
            severityBadge.classList.add('bg-orange-100', 'border-orange-200', 'text-orange-700');
        } else {
            severityBadge.classList.add('bg-yellow-100', 'border-yellow-200', 'text-yellow-700');
        }

        resultsContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
});

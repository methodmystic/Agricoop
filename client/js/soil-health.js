document.addEventListener('DOMContentLoaded', () => {
    const soilForm = document.getElementById('soil-form');
    const resultsContainer = document.getElementById('results-container');
    const healthStatus = document.getElementById('health-status');
    const recTitle = document.getElementById('rec-title');
    const recDesc = document.getElementById('rec-desc');
    const actionInsight = document.getElementById('action-insight');
    const weatherContextEl = document.getElementById('weather-context');
    const roiForecastEl = document.getElementById('roi-forecast');
    let nutrientChart;

    const cropNutrients = {
        wheat: { n: 120, p: 60, k: 40, ph: { min: 6.0, max: 7.5 } },
        rice: { n: 100, p: 40, k: 30, ph: { min: 5.5, max: 6.5 } },
        maize: { n: 150, p: 80, k: 60, ph: { min: 5.8, max: 7.0 } },
        cotton: { n: 110, p: 50, k: 50, ph: { min: 6.5, max: 8.0 } },
        soybean: { n: 40, p: 60, k: 40, ph: { min: 6.0, max: 7.0 } }
    };

    const getRecommendation = (n, p, k, ph, crop) => {
        const target = cropNutrients[crop];
        const diffN = target.n - n;
        const diffP = target.p - p;
        const diffK = target.k - k;

        let recommendation = "";
        let title = "";
        let insight = "";

        if (diffN > 20) {
            title = "High Nitrogen Boost Required";
            recommendation = `Apply Urea or Ammonium Nitrate to address the large Nitrogen deficit of ${Math.round(diffN)} mg/kg. Combine with organic mulch for better retention.`;
            insight = "Nitrogen is critical for vegetative growth in your selection.";
        } else if (diffP > 15) {
            title = "Phosphorus Enrichment Needed";
            recommendation = `Use Diammonium Phosphate (DAP) or Single Super Phosphate. Your soil currently lacks the ${Math.round(diffP)} mg/kg needed for strong root development.`;
            insight = "Phosphorus deficiency often leads to purple tints on leaves.";
        } else if (diffK > 15) {
            title = "Potassium Supplement Required";
            recommendation = `Muriate of Potash (MOP) is recommended. Increasing Potassium by ${Math.round(diffK)} mg/kg will improve drought resistance and crop quality.`;
            insight = "Potassium acts as a 'regulator' for water movement within plants.";
        } else {
            title = "Balanced Maintenance Protocol";
            recommendation = "Your nutrient levels are within 10% of optimal. Use a balanced NPK 10-10-10 organic compost to maintain this premium soil health.";
            insight = "You are in the top 5% of soil health performers in your region.";
        }

        // PH adjustments
        if (ph < target.ph.min) {
            recommendation += " Additionally, your soil is slightly acidic; consider applying agricultural lime to raise the pH.";
        } else if (ph > target.ph.max) {
            recommendation += " Your soil is alkaline; adding elemental sulfur or organic peat moss can help lower the pH level.";
        }

        return { title, recommendation, insight };
    };

    const updateChart = (n, p, k, target) => {
        const ctx = document.getElementById('nutrientChart').getContext('2d');
        
        if (nutrientChart) {
            nutrientChart.destroy();
        }

        nutrientChart = new Chart(ctx, {
            type: 'radar',
            data: {
                labels: ['Nitrogen (N)', 'Phosphorus (P)', 'Potassium (K)'],
                datasets: [{
                    label: 'Current Soil Levels',
                    data: [n, p, k],
                    backgroundColor: 'rgba(16, 185, 129, 0.2)',
                    borderColor: '#10b981',
                    borderWidth: 3,
                    pointBackgroundColor: '#10b981',
                    fill: true
                }, {
                    label: 'Optimal Levels',
                    data: [target.n, target.p, target.k],
                    backgroundColor: 'rgba(251, 191, 36, 0.1)',
                    borderColor: '#fbbf24',
                    borderWidth: 2,
                    borderDash: [5, 5],
                    pointBackgroundColor: '#fbbf24',
                    fill: true
                }]
            },
            options: {
                scales: {
                    r: {
                        beginAtZero: true,
                        grid: { color: 'rgba(0, 0, 0, 0.05)' },
                        angleLines: { display: false },
                        ticks: { display: false }
                    }
                },
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 20,
                            font: { family: 'Outfit', weight: 'bold' }
                        }
                    }
                },
                maintainAspectRatio: false
            }
        });
    };

    soilForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const n = parseFloat(document.getElementById('nitrogen').value);
        const p = parseFloat(document.getElementById('phosphorus').value);
        const k = parseFloat(document.getElementById('potassium').value);
        const ph = parseFloat(document.getElementById('ph').value);
        const crop = document.getElementById('crop').value;
        const temp = parseFloat(document.getElementById('temp').value);
        const humidity = parseFloat(document.getElementById('humidity').value);

        // Show results with animation
        resultsContainer.classList.remove('opacity-0', 'translate-y-10');
        resultsContainer.classList.add('opacity-100', 'translate-y-0');

        const { title, recommendation, insight } = getRecommendation(n, p, k, ph, crop);
        const target = cropNutrients[crop];

        // Weather Context Logic
        let weatherMessage = "";
        if (temp > 35) {
            weatherMessage = `High temperature (<span class="font-bold text-dark">${temp}°C</span>) detected. Apply fertilizer during early morning or late evening to prevent volatilization.`;
        } else if (temp < 15) {
            weatherMessage = `Low temperature (<span class="font-bold text-dark">${temp}°C</span>) may slow nutrient absorption. Consider slow-release variants.`;
        } else if (humidity > 80) {
            weatherMessage = `High humidity (<span class="font-bold text-dark">${humidity}%</span>) suggests waiting for a dry window to prevent nutrient runoff and fungal risks.`;
        } else {
            weatherMessage = `Ideal climate conditions (<span class="font-bold text-dark">${temp}°C, ${humidity}% humidity</span>). Optimal fertilization window is within the next 48 hours.`;
        }

        // ROI Forecast Logic
        let roiBase = 10000;
        if (crop === 'wheat' || crop === 'rice') roiBase = 12000;
        if (crop === 'cotton') roiBase = 18000;
        if (crop === 'soybean') roiBase = 15000;
        
        const nutrientDeficitSeverity = Math.abs(target.n - n) + Math.abs(target.p - p) + Math.abs(target.k - k);
        const roiEstimate = roiBase + (nutrientDeficitSeverity * 15);

        // Update UI
        healthStatus.textContent = "Analysis Complete";
        recTitle.textContent = title;
        recDesc.textContent = recommendation;
        actionInsight.textContent = `"${insight}"`;
        weatherContextEl.innerHTML = weatherMessage;
        roiForecastEl.innerHTML = `Applying this recommendation is expected to increase revenue by <span class="font-bold text-secondary">₹${Math.round(roiEstimate).toLocaleString()} per acre</span>.`;

        updateChart(n, p, k, target);

        // Smooth scroll to results
        resultsContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
    });
});

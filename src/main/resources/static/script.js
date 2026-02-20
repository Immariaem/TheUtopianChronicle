// Game state
let gameState = {
    inventory: [],
    hydration: 20,
    saturation: 20
};

// DOM elements
const outputDiv = document.getElementById('output');
const scrollContainer = document.querySelector('.page-left-content');
const commandInput = document.getElementById('command-input');
const inventorySlots = document.querySelectorAll('.inventory-slot');
const bookBg = document.getElementById('book-bg');
const btnHome = document.getElementById('btn-home');
const btnMap = document.getElementById('btn-map');

// Initialize game on page load
document.addEventListener('DOMContentLoaded', () => {
    fetchStatus();
    commandInput.addEventListener('keypress', handleCommand);

    btnHome.addEventListener('click', () => {
        bookBg.src = 'images/book.png';
        document.querySelector('.book').classList.remove('map-view');
    });

    btnMap.addEventListener('click', () => {
        bookBg.src = 'images/bookMapView.png';
        document.querySelector('.book').classList.add('map-view');
    });
});

// Fetch initial game status
async function fetchStatus() {
    try {
        const response = await fetch('/api/game/status');
        const data = await response.json();
        updateUI(data);
    } catch (error) {
        outputDiv.innerHTML = '<p>Error connecting to server.</p>';
    }
}

// Handle command input
async function handleCommand(event) {
    if (event.key !== 'Enter') return;

    const input = commandInput.value.trim();
    if (!input) return;

    commandInput.value = '';

    // Show the player's command in the output
    const escaped = input.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    outputDiv.innerHTML += `<p class="command">&gt; ${escaped}</p>`;
    scrollContainer.scrollTop = scrollContainer.scrollHeight;

    try {
        const response = await fetch(`/api/game/command?input=${encodeURIComponent(input)}`, {
            method: 'POST'
        });
        const data = await response.json();
        appendResponse(data);
    } catch (error) {
        outputDiv.innerHTML += '<p>Error sending command.</p>';
    }
}

// Update all UI elements
function updateUI(data) {
    outputDiv.innerHTML = `
        <p class="location-title">${data.quadrant}</p>
        <p class="location-text">${data.description.replace(/\n/g, '<br>')}</p>
    `;
    scrollContainer.scrollTop = scrollContainer.scrollHeight;
    updateInventory(data.inventory);
    updateStats(data.hydration, data.saturation);
    if (data.discoveredAreas) updateMap(data.discoveredAreas);
}

// Append response after a command
function appendResponse(data) {
    scrollContainer.classList.remove('game-over');

    if (data.reset) {
        const rawDesc = data.checkpoint ? data.description.substring(11) : data.description;
        const cause = rawDesc.includes('dehydration') ? 'You died of thirst' : 'You starved to death';
        const checkpointLine = data.checkpoint
            ? `<p class="location-text">You have been returned to your last checkpoint.</p>`
            : '';
        outputDiv.innerHTML = `
            <p class="game-over-cause">${cause}</p>
            ${checkpointLine}
            <p class="location-title">${data.quadrant}</p>
            <p class="location-text">${data.quadrantDescription}</p>
        `;
        scrollContainer.classList.add('game-over');
        updateInventory(data.inventory);
        updateStats(data.hydration, data.saturation);
        if (data.discoveredAreas) updateMap(data.discoveredAreas);
        scrollContainer.scrollTop = 0;
        return;
    } else {
        if (data.description.startsWith('WIN:')) {
            const endingText = data.description.substring(4);

            document.body.classList.add('win-mode');
            commandInput.disabled = true;
            outputDiv.innerHTML = '';

            const creditsHTML = `
                <div class="credits-overlay">
                    <div class="credits-ending">${endingText.replace(/\n/g, '<br>')}</div>
                    <div class="credits-scroll">
                        <p class="credits-production">A Project Code Production</p>
                        <div class="credits-spacer"></div>
                        <p class="credits-presents">presents</p>
                        <div class="credits-spacer"></div>
                        <p class="credits-main-title">The Utopian Chronicle</p>
                        <div class="credits-spacer large"></div>
                        <p class="credits-role">Written & Designed by</p>
                        <div class="credits-spacer small"></div>
                        <p class="credits-name">Maria Emmerich</p>
                        <div class="credits-spacer large"></div>
                        <p class="credits-role">Developed by</p>
                        <div class="credits-spacer small"></div>
                        <p class="credits-name">Maria Emmerich</p>
                        <p class="credits-note">with a great deal of help</p>
                        <div class="credits-spacer large"></div>
                        <p class="credits-role">Special Thanks to</p>
                        <div class="credits-spacer small"></div>
                        <p class="credits-name">My Dad</p>
                        <div class="credits-spacer small"></div>
                        <p class="credits-name">My Family</p>
                        <div class="credits-spacer small"></div>
                        <p class="credits-name">My Girlfriend & My Friends</p>
                        <div class="credits-spacer small"></div>
                        <p class="credits-name">Claude</p>
                        <div class="credits-spacer small"></div>
                        <p class="credits-note">And everyone who believed in this<br>before it believed in itself</p>
                        <div class="credits-spacer large"></div>
                        <p class="credits-quote">"Utopia is not a place to be found.<br>It is the moment you stop searching<br>and realise you were already there."</p>
                        <div class="credits-spacer large"></div>
                        <p class="credits-year">© 2026 Project Code</p>
                        <div class="credits-spacer large"></div>
                    </div>
                </div>
            `;

            document.querySelector('.book').insertAdjacentHTML('beforeend', creditsHTML);

            document.querySelector('.credits-scroll').addEventListener('animationend', () => {
                document.querySelector('.command-wrapper').innerHTML =
                    '<button class="play-again-btn" onclick="location.reload()">Play Again</button>';
            });

            return;
        }

        const isEphemeral = data.description.startsWith('EPHEMERAL:');
        const displayText = isEphemeral ? data.description.substring(10) : data.description;

        outputDiv.innerHTML += `
            <p class="location-title">${data.quadrant}</p>
            <p class="location-text">${displayText.replace(/\n/g, '<br>')}</p>
        `;

    if (isEphemeral) {
        const ephermalParagraph = outputDiv.lastElementChild;
        setTimeout(() => {
            ephermalParagraph.innerHTML = '<em>The vision fades. You must remember the way.</em>';
        }, 30000);
    }
}

    scrollContainer.scrollTop = scrollContainer.scrollHeight;
    updateInventory(data.inventory);
    updateStats(data.hydration, data.saturation);
    if (data.discoveredAreas) updateMap(data.discoveredAreas);
}

// Pixel-art SVG icons for inventory items

const itemIcons = {
    'Wild Berries': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="4" width="2" height="2" fill="#15803d"/><rect x="5" y="6" width="3" height="3" fill="#4338ca"/><rect x="6" y="7" width="1" height="1" fill="#818cf8"/><rect x="8" y="7" width="3" height="3" fill="#3730a3"/><rect x="6" y="9" width="3" height="3" fill="#4f46e5"/></svg>',
    'Fresh Berries': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="3" width="2" height="2" fill="#16a34a"/><rect x="6" y="5" width="4" height="4" fill="#dc2626"/><rect x="7" y="6" width="1" height="1" fill="#fca5a5"/><rect x="4" y="8" width="3" height="3" fill="#b91c1c"/><rect x="9" y="8" width="3" height="3" fill="#ef4444"/></svg>',
    'Mountain Spring': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="2" width="2" height="2" fill="#0ea5e9"/><rect x="6" y="4" width="4" height="2" fill="#0ea5e9"/><rect x="5" y="6" width="6" height="5" fill="#0ea5e9"/><rect x="6" y="11" width="4" height="2" fill="#0ea5e9"/><rect x="7" y="3" width="1" height="1" fill="rgba(255,255,255,0.3)"/><rect x="6" y="5" width="1" height="3" fill="#7dd3fc"/><rect x="10" y="6" width="1" height="5" fill="#0369a1"/><rect x="6" y="12" width="4" height="1" fill="#0369a1"/></svg>',
    'Stream Water': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="2" width="2" height="2" fill="#0ea5e9"/><rect x="6" y="4" width="4" height="2" fill="#0ea5e9"/><rect x="5" y="6" width="6" height="5" fill="#0ea5e9"/><rect x="6" y="11" width="4" height="2" fill="#0ea5e9"/><rect x="7" y="3" width="1" height="1" fill="rgba(255,255,255,0.3)"/><rect x="6" y="5" width="1" height="3" fill="#7dd3fc"/><rect x="10" y="6" width="1" height="5" fill="#0369a1"/><rect x="6" y="12" width="4" height="1" fill="#0369a1"/></svg>',
    'Spring Water': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="2" width="2" height="2" fill="#0ea5e9"/><rect x="6" y="4" width="4" height="2" fill="#0ea5e9"/><rect x="5" y="6" width="6" height="5" fill="#0ea5e9"/><rect x="6" y="11" width="4" height="2" fill="#0ea5e9"/><rect x="7" y="3" width="1" height="1" fill="rgba(255,255,255,0.3)"/><rect x="6" y="5" width="1" height="3" fill="#7dd3fc"/><rect x="10" y="6" width="1" height="5" fill="#0369a1"/><rect x="6" y="12" width="4" height="1" fill="#0369a1"/></svg>',
    'Glowing Mushrooms': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="9" width="2" height="4" fill="#71717a"/><rect x="5" y="6" width="6" height="3" fill="#34d399"/><rect x="6" y="5" width="4" height="1" fill="#6ee7b7"/><rect x="5" y="8" width="6" height="1" fill="#059669"/><rect x="10" y="10" width="2" height="3" fill="#52525b"/><rect x="10" y="10" width="2" height="1" fill="#047857"/></svg>',
    'Crystal Water': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="1" width="2" height="2" fill="#d8b4fe"/><rect x="6" y="4" width="4" height="2" fill="rgba(255,255,255,0.2)"/><rect x="4" y="6" width="8" height="6" fill="rgba(168,85,247,0.5)"/><rect x="5" y="7" width="6" height="4" fill="#9333ea"/><rect x="6" y="7" width="1" height="1" fill="#d8b4fe"/><rect x="7" y="13" width="2" height="1" fill="rgba(255,255,255,0.2)"/></svg>',
    'Forest Mushrooms': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="6" y="8" width="2" height="5" fill="#ffedd5"/><rect x="4" y="5" width="6" height="3" fill="#9a3412"/><rect x="5" y="4" width="4" height="1" fill="#c2410c"/><rect x="5" y="6" width="1" height="1" fill="#ea580c"/><rect x="9" y="10" width="2" height="3" fill="#fed7aa"/><rect x="8" y="9" width="4" height="1" fill="#7c2d12"/></svg>',
    'Travelers Rations': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="3" y="6" width="10" height="6" fill="#57534e"/><rect x="3" y="6" width="10" height="1" fill="#78716c"/><rect x="6" y="5" width="4" height="8" fill="#92400e"/><rect x="6" y="8" width="4" height="1" fill="#78350f"/></svg>',
    'Bread Loaf': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="3" y="6" width="10" height="6" fill="#fb923c"/><rect x="4" y="5" width="8" height="1" fill="#fdba74"/><rect x="3" y="11" width="10" height="1" fill="#ea580c"/><rect x="5" y="7" width="1" height="4" fill="#f97316"/><rect x="8" y="6" width="1" height="5" fill="#f97316"/><rect x="11" y="7" width="1" height="3" fill="#f97316"/></svg>',
    'Dried Fruits': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="4" y="6" width="8" height="7" fill="#92400e"/><rect x="5" y="7" width="6" height="5" fill="#b45309"/><rect x="6" y="5" width="4" height="1" fill="#78350f"/><rect x="5" y="4" width="6" height="1" fill="#92400e"/><rect x="7" y="9" width="2" height="2" fill="#7c2d12"/></svg>',
    'Water Skin': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="5" y="5" width="6" height="8" fill="#9a3412"/><rect x="4" y="6" width="1" height="6" fill="#9a3412"/><rect x="7" y="3" width="2" height="2" fill="#a1a1aa"/><rect x="6" y="4" width="1" height="2" fill="#7f1d1d"/><rect x="6" y="7" width="2" height="4" fill="#c2410c"/></svg>',
    'Cactus Water': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="6" y="4" width="4" height="9" fill="#4d7c0f"/><rect x="6" y="4" width="4" height="1" fill="#84cc16"/><rect x="7" y="5" width="2" height="7" fill="#65a30d"/><rect x="5" y="7" width="1" height="1" fill="rgba(0,0,0,0.4)"/><rect x="10" y="9" width="1" height="1" fill="rgba(0,0,0,0.4)"/><rect x="5" y="10" width="1" height="1" fill="rgba(0,0,0,0.4)"/><rect x="7" y="3" width="2" height="1" fill="#67e8f9"/></svg>',
    'Clear Pool': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7"y="2" width="2" height="2" fill="#0ea5e9"/><rect x="6" y="4" width="4"height="2" fill="#0ea5e9"/><rect x="5" y="6" width="6" height="5"fill="#0ea5e9"/><rect x="6" y="11" width="4" height="2" fill="#0ea5e9"/><rectx="7" y="3" width="1" height="1" fill="rgba(255,255,255,0.3)"/><rect x="6"y="5" width="1" height="3" fill="#7dd3fc"/><rect x="10" y="6" width="1"height="5" fill="#0369a1"/><rect x="6" y="12" width="4" height="1"fill="#0369a1"/></svg>',
    'Water Flask': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="6" y="4" width="4" height="9" fill="#a1a1aa"/><rect x="5" y="5" width="6" height="7" fill="#a1a1aa"/><rect x="7" y="2" width="2" height="2" fill="#52525b"/><rect x="6" y="5" width="1" height="7" fill="#d4d4d8"/><rect x="8" y="7" width="1" height="2" fill="#71717a"/></svg>',
    'Cactus Fruit': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="6" y="5" width="4" height="7" fill="#f43f5e"/><rect x="5" y="6" width="6" height="5" fill="#f43f5e"/><rect x="7" y="4" width="2" height="1" fill="#15803d"/><rect x="6" y="7" width="1" height="1" fill="#fda4af"/><rect x="9" y="9" width="1" height="1" fill="#be123c"/></svg>',
    'Dates': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="4" y="6" width="3" height="4" fill="#78350f"/><rect x="5" y="7" width="1" height="2" fill="#b45309"/><rect x="8" y="5" width="3" height="4" fill="#451a03"/><rect x="9" y="6" width="1" height="2" fill="#92400e"/><rect x="6" y="9" width="3" height="4" fill="#78350f"/></svg>',
    'Seaweed': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="5" y="11" width="2" height="3" fill="#115e59"/><rect x="6" y="8" width="2" height="3" fill="#0f766e"/><rect x="5" y="5" width="2" height="3" fill="#0d9488"/><rect x="6" y="2" width="2" height="3" fill="#14b8a6"/><rect x="9" y="10" width="2" height="4" fill="#115e59"/><rect x="10" y="7" width="2" height="3" fill="#0f766e"/></svg>',
    'Coconut': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="5" y="4" width="6" height="8" fill="#292524"/><rect x="4" y="5" width="8" height="6" fill="#292524"/><rect x="5" y="5" width="6" height="6" fill="#57534e"/><rect x="7" y="6" width="1" height="1" fill="#000"/><rect x="9" y="6" width="1" height="1" fill="#000"/><rect x="8" y="8" width="1" height="1" fill="#000"/></svg>',
    'Bananas': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="8" y="3" width="2" height="2" fill="#166534"/><rect x="6" y="5" width="2" height="8" fill="#facc15"/><rect x="8" y="5" width="2" height="8" fill="#eab308"/><rect x="10" y="5" width="2" height="6" fill="#ca8a04"/><rect x="6" y="13" width="1" height="1" fill="rgba(0,0,0,0.3)"/><rect x="8" y="13" width="1" height="1" fill="rgba(0,0,0,0.3)"/></svg>',
    'Coconut Water': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="5" y="7" width="6" height="5" fill="#44403c"/><rect x="4" y="8" width="8" height="3" fill="#44403c"/><rect x="5" y="7" width="6" height="1" fill="#d6d3d1"/><rect x="8" y="3" width="1" height="5" fill="#fef08a"/></svg>',
    'Fathers Compass': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="1" width="2" height="2" fill="#a16207"/><rect x="4" y="4" width="8" height="8" fill="#ca8a04"/><rect x="5" y="5" width="6" height="6" fill="#fff"/><rect x="7" y="7" width="2" height="2" fill="#27272a"/><rect x="7" y="5" width="2" height="2" fill="#ef4444"/><rect x="7" y="9" width="2" height="2" fill="#a1a1aa"/></svg>',
    'Zaras Cargo': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="3" y="3" width="10" height="10" fill="#78350f"/><rect x="4" y="4" width="8" height="8" fill="#92400e"/><rect x="5" y="5" width="6" height="1" fill="#b45309"/><rect x="5" y="7" width="6" height="1" fill="#b45309"/><rect x="5" y="9" width="6" height="1" fill="#b45309"/></svg>',
    'Hermits Journal': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="4" y="3" width="8" height="10" fill="#7f1d1d"/><rect x="3" y="3" width="1" height="10" fill="#450a0a"/><rect x="11" y="4" width="2" height="8" fill="#fef9c3"/><rect x="6" y="5" width="4" height="1" fill="#eab308"/><rect x="6" y="9" width="4" height="1" fill="#eab308"/></svg>',
    'Blue Crystal Key': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="5" width="2" height="9" fill="#52525b"/><rect x="9" y="11" width="2" height="1" fill="#52525b"/><rect x="9" y="13" width="2" height="1" fill="#52525b"/><rect x="6" y="1" width="4" height="4" fill="#0ea5e9"/><rect x="7" y="2" width="2" height="2" fill="#7dd3fc"/></svg>',
    'Red Crystal Key': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="5" width="2" height="9" fill="#52525b"/><rect x="9" y="11" width="2" height="1" fill="#52525b"/><rect x="9" y="13" width="2" height="1" fill="#52525b"/><rect x="6" y="1" width="4" height="4" fill="#f43f5e"/><rect x="7" y="2" width="2" height="2" fill="#fda4af"/></svg>',
    'Green Crystal Key': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="5" width="2" height="9" fill="#52525b"/><rect x="9" y="11" width="2" height="1" fill="#52525b"/><rect x="9" y="13" width="2" height="1" fill="#52525b"/><rect x="6" y="1" width="4" height="4" fill="#10b981"/><rect x="7" y="2" width="2" height="2" fill="#6ee7b7"/></svg>',
    'Kiras Crystal': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="3" width="2" height="10" fill="#d946ef"/><rect x="6" y="5" width="1" height="6" fill="#9333ea"/><rect x="9" y="5" width="1" height="6" fill="#e879f9"/><rect x="7" y="4" width="1" height="3" fill="rgba(255,255,255,0.6)"/><rect x="5" y="8" width="1" height="3" fill="#a855f7"/><rect x="10" y="7" width="1" height="4" fill="#c026d3"/><rect x="4" y="4" width="1" height="1" fill="#67e8f9"/><rect x="11" y="12" width="1" height="1" fill="#67e8f9"/></svg>',
    'Fountain Water': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="2" width="2" height="2" fill="#0ea5e9"/><rect x="6" y="4" width="4" height="2" fill="#0ea5e9"/><rect x="5" y="6" width="6" height="5" fill="#0ea5e9"/><rect x="6" y="11" width="4" height="2" fill="#0ea5e9"/><rect x="7" y="3" width="1" height="1" fill="rgba(255,255,255,0.3)"/><rect x="6" y="5" width="1" height="3" fill="#7dd3fc"/><rect x="10" y="6" width="1" height="5" fill="#0369a1"/><rect x="6" y="12" width="4" height="1" fill="#0369a1"/></svg>',
    'Sacred Water': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="2" width="2" height="2" fill="#0ea5e9"/><rect x="6" y="4" width="4" height="2" fill="#0ea5e9"/><rect x="5" y="6" width="6" height="5" fill="#0ea5e9"/><rect x="6" y="11" width="4" height="2" fill="#0ea5e9"/><rect x="7" y="3" width="1" height="1" fill="rgba(255,255,255,0.3)"/><rect x="6" y="5" width="1" height="3" fill="#7dd3fc"/><rect x="10" y="6" width="1" height="5" fill="#0369a1"/><rect x="6" y="12" width="4" height="1" fill="#0369a1"/></svg>',
    'Sweet Water': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="2" width="2" height="2" fill="#0ea5e9"/><rect x="6" y="4" width="4" height="2" fill="#0ea5e9"/><rect x="5" y="6" width="6" height="5" fill="#0ea5e9"/><rect x="6" y="11" width="4" height="2" fill="#0ea5e9"/><rect x="7" y="3" width="1" height="1" fill="rgba(255,255,255,0.3)"/><rect x="6" y="5" width="1" height="3" fill="#7dd3fc"/><rect x="10" y="6" width="1" height="5" fill="#0369a1"/><rect x="6" y="12" width="4" height="1" fill="#0369a1"/></svg>',
    'Vent Water': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="2" width="2" height="2" fill="#0ea5e9"/><rect x="6" y="4" width="4" height="2" fill="#0ea5e9"/><rect x="5" y="6" width="6" height="5" fill="#0ea5e9"/><rect x="6" y="11" width="4" height="2" fill="#0ea5e9"/><rect x="7" y="3" width="1" height="1" fill="rgba(255,255,255,0.3)"/><rect x="6" y="5" width="1" height="3" fill="#7dd3fc"/><rect x="10" y="6" width="1" height="5" fill="#0369a1"/><rect x="6" y="12" width="4" height="1" fill="#0369a1"/></svg>',
    'Shell Water': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="2" width="2" height="2" fill="#0ea5e9"/><rect x="6" y="4" width="4" height="2" fill="#0ea5e9"/><rect x="5" y="6" width="6" height="5" fill="#0ea5e9"/><rect x="6" y="11" width="4" height="2" fill="#0ea5e9"/><rect x="7" y="3" width="1" height="1" fill="rgba(255,255,255,0.3)"/><rect x="6" y="5" width="1" height="3" fill="#7dd3fc"/><rect x="10" y="6" width="1" height="5" fill="#0369a1"/><rect x="6" y="12" width="4" height="1" fill="#0369a1"/></svg>',
    'Sea Grapes': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="2" width="2" height="12" fill="#065f46"/><rect x="5" y="4" width="2" height="2" fill="#84cc16"/><rect x="9" y="5" width="2" height="2" fill="#65a30d"/><rect x="4" y="7" width="3" height="3" fill="#84cc16"/><rect x="9" y="8" width="3" height="3" fill="#65a30d"/><rect x="6" y="10" width="3" height="3" fill="#84cc16"/></svg>',
    'Luminous Kelp': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="5" y="11" width="2" height="3" fill="#115e59"/><rect x="6" y="8" width="2" height="3" fill="#0f766e"/><rect x="5" y="5" width="2" height="3" fill="#0d9488"/><rect x="6" y="2" width="2" height="3" fill="#14b8a6"/><rect x="9" y="10" width="2" height="4" fill="#115e59"/><rect x="10" y="7" width="2" height="3" fill="#0f766e"/></svg>',
    'Coral Relic': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="7" y="11" width="2" height="2" fill="#7c2d12"/><rect x="6" y="12" width="4" height="1" fill="#9a3412"/><rect x="7" y="6" width="2" height="5" fill="#ea580c"/><rect x="4" y="5" width="2" height="3" fill="#f43f5e"/><rectx="6" y="7" width="1" height="2" fill="#f97316"/><rect x="9" y="4" width="2" height="4" fill="#f43f5e"/><rect x="9" y="8" width="1" height="1" fill="#f97316"/><rect x="4" y="4" width="1" height="1" fill="#fda4af"/><rectx="10" y="3" width="1" height="1" fill="#fda4af"/><rect x="7" y="5" width="1" height="1" fill="#fb7185"/></svg>',
    'Pearl Relic': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="6" y="12" width="4" height="1" fill="rgba(39,39,42,0.5)"/><rect x="5" y="4" width="6" height="8" fill="#e4e4e7"/><rect x="4" y="5" width="8" height="6" fill="#e4e4e7"/><rect x="5" y="9" width="6" height="3" fill="#d4d4d8"/><rectx="4" y="8" width="1" height="3" fill="#d4d4d8"/><rect x="11" y="8" width="1"height="3" fill="#d4d4d8"/><rect x="6" y="10" width="4" height="2"fill="#a1a1aa"/><rect x="6" y="5" width="2" height="2" fill="#ffffff"/><rectx="5" y="6" width="1" height="1" fill="#ffffff"/></svg>',
    'Stone Relic': '<svg viewBox="0 0 16 16" class="slot-item-icon"><rect x="4"y="5" width="8" height="8" fill="#57534e"/><rect x="5" y="4" width="6"height="10" fill="#57534e"/><rect x="6" y="3" width="4" height="1"fill="#78716c"/><rect x="5" y="12" width="6" height="2" fill="#44403c"/><rectx="11" y="6" width="1" height="6" fill="#44403c"/><rect x="7" y="6" width="2"height="5" fill="#292524"/><rect x="6" y="8" width="4" height="1"fill="#292524"/><rect x="6" y="6" width="1" height="1" fill="#a8a29e"/><rectx="9" y="6" width="1" height="1" fill="#a8a29e"/><rect x="6" y="10" width="1"height="1" fill="#a8a29e"/><rect x="9" y="10" width="1" height="1"fill="#a8a29e"/></svg>',
};

// Update inventory slots
function updateInventory(items) {
    inventorySlots.forEach((slot, index) => {
        const plusIcon = slot.querySelector('.slot-plus');
        // Remove any previous item content
        const oldIcon = slot.querySelector('.slot-item-icon');
        const oldLabel = slot.querySelector('.slot-label');
        if (oldIcon) oldIcon.remove();
        if (oldLabel) oldLabel.remove();

        if (items[index]) {
            plusIcon.style.display = 'none';
            const svgMarkup = itemIcons[items[index]];
            if (svgMarkup) {
                slot.insertAdjacentHTML('beforeend', svgMarkup);
            } else {
                // Fall back to text label for unknown items
                const label = document.createElement('span');
                label.classList.add('slot-label');
                label.textContent = items[index];
                slot.appendChild(label);
            }
        } else {
            plusIcon.style.display = 'block';
        }
    });
}

// Update map layers based on discovered areas
function updateMap(areas) {
    const areaIds = ['enchantedForest', 'mountainRange', 'nephelia', 'desertOfTruth', 'underwaterRealm', 'islandOfBliss'];
    areaIds.forEach(id => {
        const layer = document.getElementById('map-' + id);
        if (layer) {
            layer.classList.toggle('discovered', areas.includes(id));
        }
    });
}

// Update stat bars with segments
function updateStats(hydration, saturation) {
    const hydrationBar = document.getElementById('hydration-bar');
    const saturationBar = document.getElementById('saturation-bar');

    const hydrationSegments = hydrationBar.querySelectorAll('.stat-segment');
    const saturationSegments = saturationBar.querySelectorAll('.stat-segment');

    hydrationSegments.forEach((seg, i) => {
        seg.classList.toggle('active', i < hydration);
    });

    saturationSegments.forEach((seg, i) => {
        seg.classList.toggle('active', i < saturation);
    });
}

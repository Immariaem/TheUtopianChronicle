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

// Update all UI elements (initial load)
function updateUI(data) {
    outputDiv.innerHTML = `
        <p class="location-title">${data.quadrant}</p>
        <p class="location-text">${data.description.replace(/\n/g, '<br>')}</p>
    `;
    scrollContainer.scrollTop = scrollContainer.scrollHeight;
    updateInventory(data.inventory);
    updateStats(data.hydration, data.saturation);
}

// Append response after a command (keeps scroll history)
function appendResponse(data) {
    outputDiv.innerHTML += `
        <p class="location-title">${data.quadrant}</p>
        <p class="location-text">${data.description.replace(/\n/g, '<br>')}</p>
    `;
    scrollContainer.scrollTop = scrollContainer.scrollHeight;
    updateInventory(data.inventory);
    updateStats(data.hydration, data.saturation);
}

// Update inventory slots
function updateInventory(items) {
    inventorySlots.forEach((slot, index) => {
        const plusIcon = slot.querySelector('.slot-plus');
        let label = slot.querySelector('.slot-label');
        if (items[index]) {
            plusIcon.style.display = 'none';
            if (!label) {
                label = document.createElement('span');
                label.classList.add('slot-label');
                slot.appendChild(label);
            }
            label.textContent = items[index];
            label.style.display = 'block';
        } else {
            plusIcon.style.display = 'block';
            if (label) label.style.display = 'none';
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

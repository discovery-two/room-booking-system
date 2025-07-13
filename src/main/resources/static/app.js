const CONFIG = {
    cognitoDomain: 'roombook.auth.us-east-1.amazoncognito.com',
    clientId: '2i5sld7s3fno884n3jup4jrdt',
    redirectUri: window.location.origin + '/callback.html',
    apiBaseUrl: window.location.origin
};

const TokenManager = {
    get: () => localStorage.getItem('jwt_token'),
    set: (token) => localStorage.setItem('jwt_token', token),
    remove: () => localStorage.removeItem('jwt_token'),
    isValid: function () {
        const token = this.get();
        if (!token) return false;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.exp * 1000 > Date.now();
        } catch (e) {
            return false;
        }
    },
    getEmail: function () {
        const token = this.get();
        if (!token) return null;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.email || payload['cognito:username'] || 'User';
        } catch (e) {
            return null;
        }
    }
};

class ApiClient {
    constructor() {
        this.baseUrl = CONFIG.apiBaseUrl;
    }

    // Get CSRF token from cookie
    getCsrfToken() {
        const cookies = document.cookie.split('; ');
        const csrfCookie = cookies.find(row => row.startsWith('XSRF-TOKEN='));
        return csrfCookie ? decodeURIComponent(csrfCookie.split('=')[1]) : null;
    }

    async makeRequest(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;

        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
            }
        };

        // Add JWT token if available
        const token = TokenManager.get();
        if (token) {
            defaultOptions.headers['Authorization'] = `Bearer ${token}`;
        }

        // Add CSRF token for state-changing requests
        const method = options.method || 'GET';
        if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method.toUpperCase())) {
            const csrfToken = this.getCsrfToken();
            if (csrfToken) {
                defaultOptions.headers['X-XSRF-TOKEN'] = csrfToken;
            }
        }

        const finalOptions = {
            ...defaultOptions,
            ...options,
            headers: {
                ...defaultOptions.headers,
                ...options.headers
            }
        };

        try {
            const response = await fetch(url, finalOptions);

            if (response.status === 204) {
                return null;
            }

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP ${response.status}: ${errorText}`);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            } else {
                return await response.text();
            }
        } catch (error) {
            console.error('API request failed:', error);
            throw error;
        }
    }

    async getRooms() {
        return await this.makeRequest('/room');
    }

    async getRoom(id) {
        return await this.makeRequest(`/room/${id}`);
    }

    async createRoom(roomData) {
        return await this.makeRequest('/room', {
            method: 'POST',
            body: JSON.stringify(roomData)
        });
    }

    async deleteRoom(id) {
        return await this.makeRequest(`/room/${id}`, {
            method: 'DELETE'
        });
    }

    async checkRoomAvailability(roomId, startTime, endTime) {
        const params = new URLSearchParams({
            startTime: startTime,
            endTime: endTime
        });
        return await this.makeRequest(`/room/${roomId}/availability?${params}`);
    }

    async getBookings() {
        return await this.makeRequest('/booking');
    }

    async getBooking(id) {
        return await this.makeRequest(`/booking/${id}`);
    }

    async createBooking(bookingData) {
        return await this.makeRequest('/booking', {
            method: 'POST',
            body: JSON.stringify(bookingData)
        });
    }

    async deleteBooking(id) {
        return await this.makeRequest(`/booking/${id}`, {
            method: 'DELETE'
        });
    }
}

const apiClient = new ApiClient();

function login() {
    const loginUrl = `https://${CONFIG.cognitoDomain}/login?client_id=${CONFIG.clientId}&response_type=token&scope=email+openid+profile&redirect_uri=${encodeURIComponent(CONFIG.redirectUri)}`;
    window.location.href = loginUrl;
}

function logout() {
    TokenManager.remove();
    const logoutUrl = `https://${CONFIG.cognitoDomain}/logout?client_id=${CONFIG.clientId}&logout_uri=${encodeURIComponent(window.location.origin)}`;
    window.location.href = logoutUrl;
}

function showAlert(message, type = 'success') {
    const existingAlerts = document.querySelectorAll('.alert');
    existingAlerts.forEach(alert => alert.remove());

    const alertClass = type === 'error' ? 'alert-danger' : `alert-${type}`;
    const alert = document.createElement('div');
    alert.className = `alert ${alertClass} alert-dismissible fade show`;
    alert.innerHTML = `
        ${message}
        <button type="button" class="close" data-dismiss="alert">
            <span>&times;</span>
        </button>
    `;

    const activeSection = document.querySelector('.section:not([style*="display: none"])');
    if (activeSection) {
        activeSection.insertBefore(alert, activeSection.firstChild);
    }

    setTimeout(() => {
        if (alert.parentNode) {
            alert.remove();
        }
    }, 5000);
}

function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    return date.toLocaleString();
}

function showSection(sectionName) {
    const sections = document.querySelectorAll('.section');
    sections.forEach(section => section.style.display = 'none');

    const tabs = document.querySelectorAll('.nav-link');
    tabs.forEach(tab => tab.classList.remove('active'));

    const targetSection = document.getElementById(sectionName);
    const targetTab = document.getElementById(sectionName + '-tab');

    if (targetSection) targetSection.style.display = 'block';
    if (targetTab) targetTab.classList.add('active');

    if (sectionName === 'rooms') {
        loadRooms();
    } else if (sectionName === 'bookings') {
        loadBookings();
        loadRoomsForSelect();
    } else if (sectionName === 'availability') {
        loadRoomsForSelect();
    }
}

async function loadRooms() {
    try {
        const rooms = await apiClient.getRooms();
        displayRooms(rooms);
    } catch (error) {
        showAlert('Failed to load rooms: ' + error.message, 'error');
    }
}

function displayRooms(rooms) {
    const tbody = document.getElementById('rooms-tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!rooms || rooms.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">No rooms found</td></tr>';
        return;
    }

    rooms.forEach(room => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${room.id}</td>
            <td>${room.name}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="deleteRoom(${room.id})">Delete</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function handleRoomSubmit(event) {
    event.preventDefault();

    const roomName = document.getElementById('room-name').value.trim();
    if (!roomName) {
        showAlert('Please enter a room name', 'error');
        return;
    }

    try {
        await apiClient.createRoom({ name: roomName });
        showAlert('Room created successfully!', 'success');
        document.getElementById('room-form').reset();
        loadRooms();
    } catch (error) {
        showAlert('Failed to create room: ' + error.message, 'error');
    }
}

async function deleteRoom(roomId) {
    if (!confirm('Are you sure you want to delete this room?')) {
        return;
    }

    try {
        await apiClient.deleteRoom(roomId);
        showAlert('Room deleted successfully!', 'success');
        loadRooms();
    } catch (error) {
        showAlert('Failed to delete room: ' + error.message, 'error');
    }
}

async function loadBookings() {
    try {
        const bookings = await apiClient.getBookings();
        displayBookings(bookings);
    } catch (error) {
        showAlert('Failed to load bookings: ' + error.message, 'error');
    }
}

function displayBookings(bookings) {
    const tbody = document.getElementById('bookings-tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!bookings || bookings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No bookings found</td></tr>';
        return;
    }

    bookings.forEach(booking => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${booking.id}</td>
            <td>${booking.room ? booking.room.name : 'Unknown'}</td>
            <td>${formatDateTime(booking.startTime)}</td>
            <td>${formatDateTime(booking.endTime)}</td>
            <td>${booking.reservedBy}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="deleteBooking(${booking.id})">Delete</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

async function loadRoomsForSelect() {
    try {
        const rooms = await apiClient.getRooms();

        const bookingSelect = document.getElementById('booking-room');
        if (bookingSelect) {
            bookingSelect.innerHTML = '<option value="">Select a room...</option>';
            rooms.forEach(room => {
                const option = document.createElement('option');
                option.value = room.id;
                option.textContent = room.name;
                bookingSelect.appendChild(option);
            });
        }

        const availabilitySelect = document.getElementById('availability-room');
        if (availabilitySelect) {
            availabilitySelect.innerHTML = '<option value="">Select a room...</option>';
            rooms.forEach(room => {
                const option = document.createElement('option');
                option.value = room.id;
                option.textContent = room.name;
                availabilitySelect.appendChild(option);
            });
        }
    } catch (error) {
        showAlert('Failed to load rooms: ' + error.message, 'error');
    }
}

async function handleBookingSubmit(event) {
    event.preventDefault();

    const roomId = document.getElementById('booking-room').value;
    const startTime = document.getElementById('booking-start').value;
    const endTime = document.getElementById('booking-end').value;
    const reservedBy = document.getElementById('booking-reserved-by').value.trim();

    if (!roomId || !startTime || !endTime || !reservedBy) {
        showAlert('Please fill in all fields', 'error');
        return;
    }

    if (new Date(startTime) >= new Date(endTime)) {
        showAlert('End time must be after start time', 'error');
        return;
    }

    const bookingData = {
        room: { id: parseInt(roomId) },
        startTime: startTime,
        endTime: endTime,
        reservedBy: reservedBy
    };

    try {
        await apiClient.createBooking(bookingData);
        showAlert('Booking created successfully!', 'success');
        document.getElementById('booking-form').reset();
        loadBookings();
    } catch (error) {
        if (error.message.includes('409')) {
            showAlert('Booking conflict: Room is already booked for this time period', 'error');
        } else {
            showAlert('Failed to create booking: ' + error.message, 'error');
        }
    }
}

async function deleteBooking(bookingId) {
    if (!confirm('Are you sure you want to delete this booking?')) {
        return;
    }

    try {
        await apiClient.deleteBooking(bookingId);
        showAlert('Booking deleted successfully!', 'success');
        loadBookings();
    } catch (error) {
        showAlert('Failed to delete booking: ' + error.message, 'error');
    }
}

async function handleAvailabilitySubmit(event) {
    event.preventDefault();

    const roomId = document.getElementById('availability-room').value;
    const startTime = document.getElementById('availability-start').value;
    const endTime = document.getElementById('availability-end').value;

    if (!roomId || !startTime || !endTime) {
        showAlert('Please fill in all fields', 'error');
        return;
    }

    if (new Date(startTime) >= new Date(endTime)) {
        showAlert('End time must be after start time', 'error');
        return;
    }

    try {
        const isAvailable = await apiClient.checkRoomAvailability(roomId, startTime, endTime);

        const resultDiv = document.getElementById('availability-result');
        if (resultDiv) {
            if (isAvailable) {
                resultDiv.innerHTML = '<div class="alert alert-success">✅ Room is available for the selected time period!</div>';
            } else {
                resultDiv.innerHTML = '<div class="alert alert-danger">❌ Room is not available for the selected time period.</div>';
            }
        }
    } catch (error) {
        showAlert('Failed to check availability: ' + error.message, 'error');
    }
}

// Initialize CSRF token by making a request to trigger token creation
async function initializeCsrfToken() {
    try {
        // Make a simple GET request to ensure CSRF token cookie is set
        await fetch('/csrf', {
            method: 'GET',
            credentials: 'same-origin'
        });
    } catch (error) {
        console.warn('Failed to initialize CSRF token:', error);
    }
}

document.addEventListener('DOMContentLoaded', async function () {
    if (TokenManager.isValid()) {
        document.getElementById('auth-screen').style.display = 'none';
        document.getElementById('main-app').style.display = 'block';

        const email = TokenManager.getEmail();
        const usernameElement = document.getElementById('username');
        if (usernameElement && email) {
            usernameElement.textContent = email;
        }

        // Initialize CSRF token
        await initializeCsrfToken();

        setupEventListeners();
        loadRooms();
        showSection('rooms');
    } else {
        document.getElementById('auth-screen').style.display = 'block';
        document.getElementById('main-app').style.display = 'none';
    }
});

function setupEventListeners() {
    const roomForm = document.getElementById('room-form');
    if (roomForm) {
        roomForm.addEventListener('submit', handleRoomSubmit);
    }

    const bookingForm = document.getElementById('booking-form');
    if (bookingForm) {
        bookingForm.addEventListener('submit', handleBookingSubmit);
    }

    const availabilityForm = document.getElementById('availability-form');
    if (availabilityForm) {
        availabilityForm.addEventListener('submit', handleAvailabilitySubmit);
    }
}

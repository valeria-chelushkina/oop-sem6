const API_URL = '/api/users';

export const UserApi = {
	async getAll() {
        const response = await fetch(API_URL);
        if (!response.ok) throw new Error(`Failed to fetch users: ${response.status}`);
        const data = await response.json();
        return data;
    },
    async getById(id) {
        const response = await fetch(`${API_URL}?id=${id}`);
        if (!response.ok) throw new Error(`Failed to fetch user by id: ${response.status}`);
        const data = await response.json();
        return Array.isArray(data) ? data[0] : data;
    },
    async update(payload) {
        const response = await fetch(API_URL, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });
        if (!response.ok) {
            throw new Error(`Failed to update user: ${response.status}`);
        }
        return await response.json();
    }
};
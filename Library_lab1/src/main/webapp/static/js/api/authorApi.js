const API_URL = '/api/authors';

export const AuthorApi = {
	async getAll() {
        const response = await fetch(API_URL);
        if (!response.ok) throw new Error(`Failed to fetch authors: ${response.status}`);
        const data = await response.json();
        return data;
    },
    async getById(id) {
        const response = await fetch(`${API_URL}?id=${id}`);
        if (!response.ok) throw new Error(`Failed to fetch authors by id: ${response.status}`);
        const data = await response.json();
        return Array.isArray(data) ? data[0] : data;
    },
    async create(payload) {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
	            'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });
        if(!response.ok){
            throw new Error(`Failed to create author: ${response.status}`);
        };
        return await response.json();
    },
    async update(payload) {
		const response = await fetch(API_URL, {
            method: 'PUT',
            headers: {
	            'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });
        if(!response.ok){
            throw new Error(`Failed to update author: ${response.status}`);
        };
        return await response.json();
    },
    async delete(id) {
        const response = await fetch(`${API_URL}?id=${id}`, {
            method: 'DELETE'
        });
        return response.ok;
    }
};
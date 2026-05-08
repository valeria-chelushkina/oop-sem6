const API_URL = '/api/books';

export const BookApi = {
	async getAll(params) {
        const qs = params ? `?${params.toString()}` : '';
        const response = await fetch(`${API_URL}${qs}`);
        if (!response.ok) throw new Error(`Failed to fetch books: ${response.status}`);
            return await response.json();
    },
    async getById(id) {
        const response = await fetch(`${API_URL}?id=${id}`);
        if (!response.ok) throw new Error(`Failed to fetch books by id: ${response.status}`);
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
            throw new Error(`Failed to create book: ${response.status}`);
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
            throw new Error(`Failed to update book: ${response.status}`);
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
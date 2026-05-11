const API_URL = '/api/loans';

export const LoanApi = {
	async getAll() {
        const response = await fetch(API_URL);
        if (!response.ok) throw new Error(`Failed to fetch loans: ${response.status}`);
        const data = await response.json();
        return data;
    },
    async getById(id) {
        const response = await fetch(`${API_URL}?id=${id}`);
        if (!response.ok) throw new Error(`Failed to fetch loans by id: ${response.status}`);
        const data = await response.json();
        return Array.isArray(data) ? data[0] : data;
    },
    async checkActiveLoan(bookId, readerId) {
        const response = await fetch(`${API_URL}?bookId=${bookId}&readerId=${readerId}`);
        if (!response.ok) throw new Error(`Failed to check active loan: ${response.status}`);
        return await response.json();
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
            throw new Error(`Failed to create loan: ${response.status}`);
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
            throw new Error(`Failed to update loan: ${response.status}`);
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
import { UserApi } from '../api/userApi.js';

let userFirstName = document.getElementById('user-first-name');
let userLastName = document.getElementById('user-last-name')
const userEmail = document.getElementById('user-email');
const userId = document.getElementById('user-id');

export function fillUserInfo(user) {
        const container = document.getElementById('profile-data-card');
        if (container) {
            userFirstName.value = user.firstName;
            userLastName.value = user.lastName;
            userEmail.value = user.email;
            userId.value = user.id;
        }
};

export async function updateUser(user) {
	if( userFirstName.value === '' || userLastName.value === '' ) {
		alert("Any fields shouldn't be empty!");
		return;
	}
	const payload = {
	id: user.id,
	firstName: userFirstName.value,
	lastName: userLastName.value,
	email: user.email,
	role: user.role,
	registrationDate: user.registrationDate
	 };
	return await UserApi.update(payload);
};
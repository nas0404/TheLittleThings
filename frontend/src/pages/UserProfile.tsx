import { useEffect, useState } from 'react';
import Button from '../components/buttons/Button';

export default function UserProfile() {
	const [user, setUser] = useState<any>(null);
	const [error, setError] = useState<string | null>(null);
	const [oldPass, setOldPass] = useState('');
	const [newPass, setNewPass] = useState('');

	useEffect(() => {
		const t = localStorage.getItem('token');
		if (!t) return;
		fetch('http://localhost:8080/api/users/me', { headers: { Authorization: `Bearer ${t}` } })
			.then((r) => r.json())
			.then(setUser)
			.catch(() => setError('Unable to fetch'));
	}, []);

	const change = async () => {
		const t = localStorage.getItem('token');
		try {
			const res = await fetch('http://localhost:8080/api/users/change-password', {
				method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${t}` },
				body: JSON.stringify({ oldPassword: oldPass, newPassword: newPass })
			});
			if (!res.ok) {
				setError(await res.text());
				return;
			}
			setError('Password changed');
		} catch (e) {
			setError('Network');
		}
	};

	const [newUsername, setNewUsername] = useState('');
	const changeUsername = async () => {
		const t = localStorage.getItem('token');
		try {
			const res = await fetch('http://localhost:8080/api/users/change-username', {
				method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${t}` },
				body: JSON.stringify({ newUsername })
			});
			if (!res.ok) {
				setError(await res.text());
				return;
			}
			const json = await res.json();
			if (json.token) {
				localStorage.setItem('token', json.token);
				setUser((u: any) => ({ ...u, username: json.username }));
				setError('Username changed');
			} else {
				setError('No token returned');
			}
		} catch (e) {
			setError('Network');
		}
	};

	const logout = async () => {
		const t = localStorage.getItem('token');
		await fetch('http://localhost:8080/api/users/logout', { method: 'POST', headers: { Authorization: `Bearer ${t}` } });
		localStorage.removeItem('token');
		window.location.href = '/';
	};

	return (
		<div className="max-w-lg mx-auto">
			<h1 className="text-2xl font-bold mb-4">User Management</h1>
			{error && <div className="text-red-600 mb-2">{error}</div>}
			{user ? (
				<div className="space-y-4">
					<div className="rounded-xl border bg-white p-4 shadow-sm">
						<div><strong>Username:</strong> {user.username}</div>
						<div><strong>Email:</strong> {user.email}</div>
						<div><strong>Last login:</strong> {user.lastLogin ? new Date(user.lastLogin).toLocaleString() : 'Never'}</div>
					</div>

					<div className="rounded-xl border bg-white p-4 shadow-sm">
						<h3 className="font-semibold mb-2">Change Username</h3>
						<input value={newUsername} onChange={(e) => setNewUsername(e.target.value)} placeholder="New username" className="w-full mb-2 rounded-md border px-2 py-1" />
						<Button onClick={changeUsername}>Change Username</Button>
					</div>

					<div className="rounded-xl border bg-white p-4 shadow-sm">
						<h3 className="font-semibold mb-2">Change Password</h3>
						<input type="password" placeholder="Current" value={oldPass} onChange={(e) => setOldPass(e.target.value)} className="w-full mb-2 rounded-md border px-2 py-1" />
						<input type="password" placeholder="New" value={newPass} onChange={(e) => setNewPass(e.target.value)} className="w-full mb-2 rounded-md border px-2 py-1" />
						<Button onClick={change}>Change Password</Button>
					</div>

					<div>
						<Button onClick={logout} className="bg-red-600">Logout</Button>
						<div className="mt-3">
							<Button onClick={async () => {
								if (!confirm('Delete your account? This is irreversible.')) return;
								const t = localStorage.getItem('token');
								try {
									const res = await fetch('http://localhost:8080/api/users/', { method: 'DELETE', headers: { Authorization: `Bearer ${t}` } });
									if (!res.ok) { setError('Delete failed'); return; }
									localStorage.removeItem('token');
									window.location.href = '/';
								} catch (e) { setError('Network'); }
							}} className="bg-red-700">Delete account</Button>
						</div>
					</div>
				</div>
			) : (
				<div>Not logged in</div>
			)}
		</div>
	);
}

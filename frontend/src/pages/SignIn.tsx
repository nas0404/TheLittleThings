import { useState } from 'react';
import Button from '../components/buttons/Button';
import { UserAPI } from '../api/users';
import { ApiError } from '../api/http';

export default function SignIn() {
	const [form, setForm] = useState({ username: '', password: '' });
	const [error, setError] = useState<string | null>(null);

	const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		setForm({ ...form, [e.target.name]: e.target.value });
	};

	// handle login form submission
	const submit = async (e: React.FormEvent) => {
		e.preventDefault();
		setError(null);
		try {
			const data = await UserAPI.login(form);
			localStorage.setItem('token', data.token);
			localStorage.setItem('username', data.username);
			// Navigate to home and refresh the page
			window.location.href = '/home';
		} catch (err) {
			if (err instanceof ApiError) {
				setError(err.message || 'Login failed');
			} else {
				setError('Network error');
			}
		}
	};

	return (
		<div className="max-w-md mx-auto">
			<h1 className="text-2xl font-bold mb-4">Sign In</h1>
			<form onSubmit={submit} className="space-y-3">
				<div>
					<label className="block text-sm font-medium">Username</label>
					<input name="username" value={form.username} onChange={onChange}
						className="mt-1 w-full rounded-md border px-3 py-2" />
				</div>
				<div>
					<label className="block text-sm font-medium">Password</label>
					<input name="password" type="password" value={form.password} onChange={onChange}
						className="mt-1 w-full rounded-md border px-3 py-2" />
				</div>
				{error && <div className="text-red-600">{error}</div>}
				<Button type="submit">Sign In</Button>
			</form>
		</div>
	);
}



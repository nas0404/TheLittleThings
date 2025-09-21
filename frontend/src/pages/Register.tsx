import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/buttons/Button";

type RegisterForm = {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  dob: string;          // yyyy-mm-dd
  gender: "" | "male" | "female" | "other";
  region: string;
};

const calcAge = (isoDate: string) => {
  if (!isoDate) return undefined;
  const today = new Date();
  const birth = new Date(isoDate);
  let age = today.getFullYear() - birth.getFullYear();
  const md = today.getMonth() - birth.getMonth();
  if (md < 0 || (md === 0 && today.getDate() < birth.getDate())) age--;
  return age;
};

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState<RegisterForm>({
    username: "",
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    dob: "",
    gender: "",
    region: "",
  });

  const age = useMemo(() => calcAge(form.dob), [form.dob]);
  const isValid =
    form.username.trim().length >= 3 &&
    /\S+@\S+\.\S+/.test(form.email) &&
    form.password.length >= 8 &&
    !!form.firstName.trim() &&
    !!form.lastName.trim() &&
    !!form.dob;
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const onChange: React.ChangeEventHandler<HTMLInputElement | HTMLSelectElement> = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const handleSubmit: React.FormEventHandler = async (e) => {
    e.preventDefault();
    console.log('Register handleSubmit triggered', { form, isValid });
    if (!isValid) {
      console.warn('Form invalid, aborting submit');
      return;
    }
    try {
      setSubmitting(true);
      const response = await fetch('http://localhost:8080/api/users/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(form)
      });

      if (response.ok) {
        setServerError(null);
        // Registration successful — parse token and store it
        let data: any = null;
        try {
          data = await response.json();
        } catch (_e) {
          // ignore
        }
        if (data && data.token) {
          localStorage.setItem('token', data.token);
          navigate("/home");
        } else {
          // If server returned 200 but no token, show message
          const text = data ? JSON.stringify(data) : 'Registration succeeded but no token received';
          setServerError(text);
        }
      } else {
        // reg errors
        let errorText = 'Registration failed';
        try {
          const json = await response.json();
          errorText = typeof json === 'string' ? json : JSON.stringify(json);
        } catch (_) {
          errorText = await response.text();
        }
        console.error('Registration failed:', errorText);
        setServerError(errorText);
      }
    } catch (error) {
      console.error('Registration error:', error);
      setServerError(String(error));
    }
    finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-10 px-4">
      <div className="w-full max-w-md">
        <h2 className="text-center text-2xl font-semibold text-gray-900">Create your account</h2>

        <form className="mt-6 space-y-5" onSubmit={handleSubmit} noValidate>
          {/* Personal info card */}
          <div className="rounded-xl border bg-white p-4 shadow-sm">
            <h3 className="text-lg font-semibold mb-3">Personal Details</h3>
            <div className="grid grid-cols-2 gap-4">
              <Field label="First name" id="firstName">
                <input
                  id="firstName" name="firstName" type="text" autoComplete="given-name" required
                  className="mt-1 w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  value={form.firstName} onChange={onChange}
                />
              </Field>
              <Field label="Last name" id="lastName">
                <input
                  id="lastName" name="lastName" type="text" autoComplete="family-name" required
                  className="mt-1 w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  value={form.lastName} onChange={onChange}
                />
              </Field>
            </div>

            <div className="grid grid-cols-2 gap-4 mt-4">
              <Field label="Date of birth" id="dob" hint={age ? `You’ll be registered as ${age}` : undefined}>
                <input
                  id="dob" name="dob" type="date" required
                  className="mt-1 w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  value={form.dob} onChange={onChange}
                />
              </Field>
              <Field label="Gender" id="gender">
                <select
                  id="gender" name="gender"
                  className="mt-1 w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  value={form.gender} onChange={onChange}
                >
                  <option value="">Select</option>
                  <option value="male">Male</option>
                  <option value="female">Female</option>
                  <option value="other">Other</option>
                </select>
              </Field>
            </div>
          </div>

          {/* Account info card */}
          <div className="rounded-xl border bg-white p-4 shadow-sm">
            <h3 className="text-lg font-semibold mb-3">Account</h3>
            <Field label="Username" id="username" hint="3+ characters, lowercase is fine.">
              <input
                id="username" name="username" type="text" required
                className="mt-1 w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={form.username} onChange={onChange}
              />
            </Field>

            <Field label="Email" id="email">
              <input
                id="email" name="email" type="email" autoComplete="email" required
                className="mt-1 w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={form.email} onChange={onChange}
              />
            </Field>

            <Field label="Password" id="password" hint="At least 8 characters.">
              <input
                id="password" name="password" type="password" autoComplete="new-password" required
                className="mt-1 w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={form.password} onChange={onChange}
              />
            </Field>
          </div>

          {/* Other info card + submit */}
          <div className="rounded-xl border bg-white p-4 shadow-sm">
            <h3 className="text-lg font-semibold mb-3">Other</h3>
            <Field label="Region" id="region">
              <input
                id="region" name="region" type="text"
                className="mt-1 w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                value={form.region} onChange={onChange}
              />
            </Field>

            <div className="mt-4">
              <ValidationHints form={form} />
              {serverError ? (
                <div className="mt-3 rounded-md bg-red-50 p-3 text-sm text-red-800">{serverError}</div>
              ) : null}
              <Button
                type="submit"
                className="w-full bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-60"
                disabled={!isValid || submitting}
                isLoading={submitting}
                title={!isValid ? 'Please fill all required fields correctly' : undefined}
              >
                Register
              </Button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

// Small UI component to render a list of validation hints
function ValidationHints({ form }: { form: RegisterForm }) {
  const hints: string[] = [];
  if (form.username.trim().length < 3) hints.push('Username must be at least 3 characters');
  if (!/\S+@\S+\.\S+/.test(form.email)) hints.push('Enter a valid email address');
  if (form.password.length < 8) hints.push('Password must be at least 8 characters');
  if (!form.firstName.trim()) hints.push('First name is required');
  if (!form.lastName.trim()) hints.push('Last name is required');
  if (!form.dob) hints.push('Date of birth is required');

  if (hints.length === 0) return null;
  return (
    <div className="mt-3 rounded-md bg-yellow-50 p-3 text-sm text-yellow-800">
      <strong>Fix the following:</strong>
      <ul className="list-disc list-inside mt-1">
        {hints.map((h) => (
          <li key={h}>{h}</li>
        ))}
      </ul>
    </div>
  );
}

/** Small presentational wrapper to keep JSX tidy */
function Field(props: { id: string; label: string; children: React.ReactNode; hint?: string }) {
  return (
    <div>
      <label htmlFor={props.id} className="block text-sm font-medium text-gray-700">{props.label}</label>
      {props.children}
      {props.hint && <p className="mt-1 text-xs text-gray-500">{props.hint}</p>}
    </div>
  );
}

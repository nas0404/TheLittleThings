import { useState, useEffect } from 'react';

interface Win {
  winId: number;
  title: string;
  description?: string;
}

interface JournalEntry {
  journalId: number;
  title: string;
  content: string;
  linkedWinId?: number;
  linkedWinTitle?: string;
  createdAt: string;
  updatedAt: string;
}

interface JournalEditFormProps {
  entry: JournalEntry;
  onCancel: () => void;
  onSuccess: () => void;
}

export default function JournalEditForm({ entry, onCancel, onSuccess }: JournalEditFormProps) {
  const [formData, setFormData] = useState({
    title: entry.title,
    content: entry.content,
    linkedWinId: entry.linkedWinId?.toString() || ''
  });
  
  const [wins, setWins] = useState<Win[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [serverError, setServerError] = useState<string | null>(null);

  useEffect(() => {
    fetchUserWins();
  }, []);

  const fetchUserWins = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) return;

      const response = await fetch('http://localhost:8080/api/journals/wins', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        const data = await response.json();
        setWins(data);
      }
    } catch (err) {
      console.error('Error fetching wins:', err);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const validateForm = () => {
    if (!formData.title.trim()) {
      setError('Title is required');
      return false;
    }
    if (!formData.content.trim()) {
      setError('Content is required');
      return false;
    }
    if (formData.title.length > 255) {
      setError('Title must be 255 characters or less');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    setError(null);
    setServerError(null);

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setError('Please log in');
        return;
      }

      const requestBody = {
        title: formData.title,
        content: formData.content,
        linkedWinId: formData.linkedWinId ? parseInt(formData.linkedWinId) : null
      };

      const response = await fetch(`http://localhost:8080/api/journals/${entry.journalId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      });

      if (response.ok) {
        onSuccess();
      } else {
        const errorData = await response.json().catch(() => ({ error: 'Failed to update journal entry' }));
        setServerError(errorData.error || 'Failed to update journal entry');
      }
    } catch (err) {
      setServerError('Failed to update journal entry');
      console.error('Error updating journal entry:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="mb-4">
        <h3 className="text-lg font-semibold text-gray-900">Edit Journal Entry</h3>
      </div>

      {/* Title Field */}
      <div>
        <label htmlFor="edit-title" className="block text-sm font-medium text-gray-700 mb-1">
          Title *
        </label>
        <input
          type="text"
          id="edit-title"
          name="title"
          value={formData.title}
          onChange={handleInputChange}
          maxLength={255}
          className="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />
        <div className="mt-1 text-xs text-gray-500">
          {formData.title.length}/255 characters
        </div>
      </div>

      {/* Content Field */}
      <div>
        <label htmlFor="edit-content" className="block text-sm font-medium text-gray-700 mb-1">
          Content *
        </label>
        <textarea
          id="edit-content"
          name="content"
          value={formData.content}
          onChange={handleInputChange}
          rows={6}
          className="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />
      </div>

      {/* Linked Win Field */}
      <div>
        <label htmlFor="edit-linkedWinId" className="block text-sm font-medium text-gray-700 mb-1">
          Link to Win (Optional)
        </label>
        <select
          id="edit-linkedWinId"
          name="linkedWinId"
          value={formData.linkedWinId}
          onChange={handleInputChange}
          className="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        >
          <option value="">No linked win</option>
          {wins.map((win) => (
            <option key={win.winId} value={win.winId}>
              {win.title}
            </option>
          ))}
        </select>
      </div>

      {/* Error Messages */}
      {error && (
        <div className="rounded-md bg-red-50 border border-red-200 p-3">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      {serverError && (
        <div className="rounded-md bg-red-50 border border-red-200 p-3">
          <p className="text-sm text-red-700">{serverError}</p>
        </div>
      )}

      {/* Action Buttons */}
      <div className="flex justify-end gap-3">
        <button
          type="button"
          onClick={onCancel}
          className="rounded-md bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200"
          disabled={loading}
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={loading}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? 'Saving...' : 'Save Changes'}
        </button>
      </div>
    </form>
  );
}
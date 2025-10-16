import { useState, useEffect } from 'react';
import { JournalAPI, type Win, type CreateJournalRequest } from '../../api/JournalApi';
import { ApiError } from '../../api/http';

interface JournalCreateFormProps {
  onSuccess: () => void;
}

export default function JournalCreateForm({ onSuccess }: JournalCreateFormProps) {
  // form data state for title, content and optional win link
  const [formData, setFormData] = useState({
    title: '',
    content: '',
    linkedWinId: ''
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
      console.log('Loading wins');
      const data = await JournalAPI.getWins();
      setWins(data);
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

  // basic form validation for checkng required fields and length
  const validateForm = () => {
    if (!formData.title.trim()) {
      setError('Title required');
      return false;
    }
    if (!formData.content.trim()) {
      setError('Content required');
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
    if (!validateForm()) return; // don't submit if invalid

    setLoading(true);
    setError(null);
    setServerError(null);

    try {
      const body: CreateJournalRequest = {
        title: formData.title,
        content: formData.content,
        ...(formData.linkedWinId && { linkedWinId: parseInt(formData.linkedWinId) })
      };

      await JournalAPI.create(body);
      onSuccess();
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 401) {
          setError('Please log in');
        } else {
          setServerError(err.message || 'Failed to create entry');
        }
      } else {
        setServerError('Network error');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="max-w-2xl space-y-6">
      <div>
        <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
          Title *
        </label>
        <input
          type="text"
          id="title"
          name="title"
          value={formData.title}
          onChange={handleInputChange}
          maxLength={255}
          className="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          placeholder="Enter a title for your journal entry"
        />
        <div className="mt-1 text-xs text-gray-500">
          {formData.title.length}/255 characters
        </div>
      </div>

      
      <div>
        <label htmlFor="content" className="block text-sm font-medium text-gray-700 mb-1">
          Content *
        </label>
        <textarea
          id="content"
          name="content"
          value={formData.content}
          onChange={handleInputChange}
          rows={8}
          className="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          placeholder="Write your journal entry here..."
        />
      </div>

      
      <div>
        <label htmlFor="linkedWinId" className="block text-sm font-medium text-gray-700 mb-1">
          Link to Win (Optional)
        </label>
        <select
          id="linkedWinId"
          name="linkedWinId"
          value={formData.linkedWinId}
          onChange={handleInputChange}
          className="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        >
          <option value="">Select a win to link...</option>
          {wins.map((win) => (
            <option key={win.winId} value={win.winId}>
              {win.title}
            </option>
          ))}
        </select>
        <div className="mt-1 text-xs text-gray-500">
          Link this journal entry to one of your wins
        </div>
      </div>

      
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

      
      <div className="flex justify-end">
        <button
          type="submit"
          disabled={loading}
          className="rounded-md bg-blue-600 px-6 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? 'Creating...' : 'Create Entry'}
        </button>
      </div>
    </form>
  );
}
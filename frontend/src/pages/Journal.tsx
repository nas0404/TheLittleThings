import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import JournalCreateForm from '../components/journals/JournalCreateForm';
import JournalEntryCard from '../components/journals/JournalEntryCard';
import { JournalAPI, type JournalEntry } from '../api/JournalApi';
import { ApiError } from '../api/http';

export default function Journal() {
  // manage view state, either showing list or create form
  const [view, setView] = useState<'list' | 'create'>('list');
  const [entries, setEntries] = useState<JournalEntry[]>([]);
  const [sortBy, setSortBy] = useState<'date' | 'title'>('date');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (view === 'list') {
      fetchEntries();
    }
  }, [view, sortBy]);

  const fetchEntries = async () => {
    setLoading(true);
    setError(null);
    
    console.log('Fetching journal entries');
    
    try {
      const data = await JournalAPI.list(sortBy);
      setEntries(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setError('Please log in first');
      } else {
        setError('Failed to load entries');
      }
      console.error('Error fetching journal entries:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleEntryCreated = () => {
    setView('list');
    fetchEntries();
  };

  const handleEntryDeleted = (deletedId: number) => {
    console.log('handleEntryDeleted called with ID:', deletedId);
    console.log('Current entries before delete:', entries.length);
    // Instead of trying to update local state, fetch fresh data from backend
    fetchEntries();
  };

  if (view === 'create') {
    return (
      <div>
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-3xl font-bold text-gray-900">Create Journal Entry</h1>
          <button
            onClick={() => setView('list')}
            className="rounded-md bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200"
          >
            ← Back to Journal
          </button>
        </div>
        <JournalCreateForm onSuccess={handleEntryCreated} />
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">My Journal</h1>
        <button
          onClick={() => setView('create')}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          Create Entry
        </button>
      </div>

      <div className="mb-4 flex items-center gap-4">
        <span className="text-sm font-medium text-gray-700">Sort by:</span>
        <button
          onClick={() => setSortBy('date')}
          className={`rounded-md px-3 py-1 text-sm ${
            sortBy === 'date'
              ? 'bg-blue-100 text-blue-700 font-medium'
              : 'text-gray-600 hover:bg-gray-100'
          }`}
        >
          Date
        </button>
        <button
          onClick={() => setSortBy('title')}
          className={`rounded-md px-3 py-1 text-sm ${
            sortBy === 'title'
              ? 'bg-blue-100 text-blue-700 font-medium'
              : 'text-gray-600 hover:bg-gray-100'
          }`}
        >
          Alphabetical
        </button>
      </div>

      {error && (
        <div className="mb-4 rounded-md bg-red-50 border border-red-200 p-4">
          <p className="text-red-700">{error}</p>
          {error.includes('log in') && (
            <Link to="/login" className="mt-2 inline-block text-red-600 underline hover:text-red-800">
              Go to Login
            </Link>
          )}
        </div>
      )}

      {loading && (
        <div className="flex justify-center py-8">
          <div className="text-gray-500">Loading journal entries...</div>
        </div>
      )}

      {!loading && !error && (
        <div>
          {entries.length === 0 ? (
            <div className="text-center py-12">
              <div className="text-gray-500 mb-4">No journal entries yet.</div>
              <button
                onClick={() => setView('create')}
                className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
              >
                Create Your First Entry
              </button>
            </div>
          ) : (
            <div className="space-y-4">
              {entries.map((entry) => (
                <JournalEntryCard
                  key={entry.journalId}
                  entry={entry}
                  onDeleted={handleEntryDeleted}
                  onUpdated={fetchEntries}
                />
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
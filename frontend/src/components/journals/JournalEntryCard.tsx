import { useState } from 'react';
import JournalEditForm from './JournalEditForm';

interface JournalEntry {
  journalId: number;
  title: string;
  content: string;
  linkedWinId?: number;
  linkedWinTitle?: string;
  createdAt: string;
  updatedAt: string;
}

interface JournalEntryCardProps {
  entry: JournalEntry;
  onDeleted: (id: number) => void;
  onUpdated: () => void;
}

export default function JournalEntryCard({ entry, onDeleted, onUpdated }: JournalEntryCardProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [loading, setLoading] = useState(false);

  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateString;
    }
  };



  const handleDelete = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      if (!token) return;

      const response = await fetch(`http://localhost:8080/api/journals/${entry.journalId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        onDeleted(entry.journalId);
      } else {
        console.error('Failed to delete journal entry');
      }
    } catch (err) {
      console.error('Error deleting journal entry:', err);
    } finally {
      setLoading(false);
      setShowDeleteConfirm(false);
    }
  };

  // truncate long content
  const contentPreview = entry.content.length > 200 
    ? entry.content.substring(0, 200) + '...' 
    : entry.content;

  if (isEditing) {
    return (
      <div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <JournalEditForm 
          entry={entry} 
          onCancel={() => setIsEditing(false)}
          onSuccess={() => {
            setIsEditing(false);
            onUpdated();
          }}
        />
      </div>
    );
  }

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm max-w-full overflow-hidden">
      {/* Header */}
      <div className="mb-4 flex items-start justify-between">
        <div className="flex-1 min-w-0">
          <h3 className="text-lg font-semibold text-gray-900 mb-1 break-words">{entry.title}</h3>
          <div className="flex items-center gap-4 text-sm text-gray-500">
            <span>Created: {formatDate(entry.createdAt)}</span>
            {entry.updatedAt !== entry.createdAt && (
              <span>Updated: {formatDate(entry.updatedAt)}</span>
            )}
          </div>
        </div>
        <div className="flex items-center gap-2 ml-4 flex-shrink-0">
          <button
            onClick={() => setIsEditing(true)}
            className="rounded-md bg-gray-100 px-3 py-1 text-sm font-medium text-gray-700 hover:bg-gray-200"
          >
            Edit
          </button>
          <button
            onClick={() => setShowDeleteConfirm(true)}
            className="rounded-md bg-red-100 px-3 py-1 text-sm font-medium text-red-700 hover:bg-red-200"
            disabled={loading}
          >
            Delete
          </button>
        </div>
      </div>

      {/* Linked Win */}
      {entry.linkedWinId && entry.linkedWinTitle && (
        <div className="mb-3 flex items-center gap-2 rounded-md bg-blue-50 border border-blue-200 px-3 py-2 max-w-full">
          <span className="text-sm font-medium text-blue-700 flex-shrink-0">🏆 Linked Win:</span>
          <span className="text-sm text-blue-600 truncate">{entry.linkedWinTitle}</span>
        </div>
      )}

      {/* Content - fixed overflow */}
      <div className="text-gray-700 max-w-full">
        <div className="whitespace-pre-wrap break-words overflow-hidden">
          {isExpanded ? entry.content : contentPreview}
        </div>
        {entry.content.length > 200 && (
          <button
            onClick={() => setIsExpanded(!isExpanded)}
            className="mt-2 text-sm text-blue-600 hover:text-blue-800"
          >
            {isExpanded ? 'Show less' : 'Show more'}
          </button>
        )}
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Delete Journal Entry</h3>
            <p className="text-gray-700 mb-4">
              Are you sure you want to delete "{entry.title}"? This action cannot be undone.
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                className="rounded-md bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200"
                disabled={loading}
              >
                Cancel
              </button>
              <button
                onClick={handleDelete}
                className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
                disabled={loading}
              >
                {loading ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
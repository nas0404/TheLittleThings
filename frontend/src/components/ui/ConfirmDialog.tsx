import React from "react";

type Props = {
  open: boolean;
  title?: string;
  message?: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void | Promise<void>;
  onCancel: () => void;
};

export default function ConfirmDialog({
  open,
  title = "Are you sure?",
  message,
  confirmText = "Delete",
  cancelText = "Cancel",
  onConfirm,
  onCancel,
}: Props) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/30" onClick={onCancel} />
      <div className="relative w-full max-w-md bg-white rounded-2xl shadow-lg p-5">
        <h3 className="text-lg font-semibold mb-2">{title}</h3>
        {message && <p className="text-sm text-slate-600 mb-4">{message}</p>}

        <div className="flex justify-end gap-2">
          <button
            type="button"
            className="px-4 py-2 rounded-lg border"
            onClick={onCancel}
          >
            {cancelText}
          </button>
          <button
            type="button"
            className="px-4 py-2 rounded-lg bg-rose-600 text-white"
            onClick={onConfirm}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

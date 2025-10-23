import { http } from "./http";

// Type representing a journal entry from the backend
export type JournalEntryDTO = {
  journalId: number;
  title: string;
  content: string;
  linkedWinId?: number;
  linkedWinTitle?: string;
  createdAt: string;
  updatedAt: string;
};

// Type representing a journal entry in the frontend
export type JournalEntry = {
  journalId: number;
  title: string;
  content: string;
  linkedWinId?: number;
  linkedWinTitle?: string;
  createdAt: string;
  updatedAt: string;
};

// Type for creating a new journal entry
export type CreateJournalRequest = {
  title: string;
  content: string;
  linkedWinId?: number;
};

// Type for updating a journal entry
export type UpdateJournalRequest = {
  title?: string;
  content?: string;
  linkedWinId?: number;
};

// Type representing a win that can be linked to a journal entry
export type Win = {
  winId: number;
  title: string;
  description?: string;
};

// Convert DTO to frontend type (currently identical, but allows for future transformations)
const toJournalEntry = (dto: JournalEntryDTO): JournalEntry => ({
  journalId: dto.journalId,
  title: dto.title,
  content: dto.content,
  linkedWinId: dto.linkedWinId,
  linkedWinTitle: dto.linkedWinTitle,
  createdAt: dto.createdAt,
  updatedAt: dto.updatedAt,
});

// Convert array of DTOs to array of frontend types
const toJournalEntryArray = (arr: JournalEntryDTO[]): JournalEntry[] => 
  arr.map(toJournalEntry);

// Journal API service
export const JournalAPI = {
  // Get all journal entries with optional sorting
  async list(sortBy?: string): Promise<JournalEntry[]> {
    const queryParam = sortBy ? `?sort=${sortBy}` : '';
    const data = await http<JournalEntryDTO[]>(`/api/journals${queryParam}`);
    return toJournalEntryArray(data);
  },

  // Create a new journal entry
  async create(body: CreateJournalRequest): Promise<JournalEntry> {
    const data = await http<JournalEntryDTO>(`/api/journals`, {
      method: "POST",
      body: JSON.stringify(body),
    });
    return toJournalEntry(data);
  },

  // Update an existing journal entry
  async update(id: number, body: UpdateJournalRequest): Promise<JournalEntry> {
    const data = await http<JournalEntryDTO>(`/api/journals/${id}`, {
      method: "PUT",
      body: JSON.stringify(body),
    });
    return toJournalEntry(data);
  },

  // Delete a journal entry
  async remove(id: number): Promise<void> {
    await http<void>(`/api/journals/${id}`, { method: "DELETE" });
  },

  // Get user's wins that can be linked to journal entries
  async getWins(): Promise<Win[]> {
    return http<Win[]>(`/api/journals/wins`);
  },
};

export default JournalAPI;
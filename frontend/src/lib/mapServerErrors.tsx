// Small helper to interpret Spring-style error payloads
export function mapServerErrors(details: any): Record<string, string> {
  const out: Record<string, string> = {};
  if (details?.errors && Array.isArray(details.errors)) {
    for (const e of details.errors) {
      const key = e.field || e.param || e.code || "";
      const msg = e.defaultMessage || e.message || details.message;
      if (key && msg && !out[key]) out[key] = msg;
    }
  }
  if (!Object.keys(out).length && details?.message) out.form = details.message;
  return out;
}
export default mapServerErrors;



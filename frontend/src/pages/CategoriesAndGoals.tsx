export default function CategoriesAndGoals() {
  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-bold">Settings</h1>
      <form className="space-y-3">
        <label className="block">
          <span className="text-sm">Display name</span>
          <input className="mt-1 w-full rounded-lg border p-2" placeholder="Your name" />
        </label>
        <button className="rounded-xl bg-black px-4 py-2 text-white">Save</button>
      </form>
    </section>
  );
}

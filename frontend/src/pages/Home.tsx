export default function Home() {
  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-bold">Welcome</h1>
      <p>Start here. Add widgets or cards that matter to your user.</p>
      <div className="grid gap-4 md:grid-cols-3">
        <div className="rounded-xl border bg-white p-4 shadow-sm">Card A</div>
        <div className="rounded-xl border bg-white p-4 shadow-sm">Card B</div>
        <div className="rounded-xl border bg-white p-4 shadow-sm">Card C</div>
      </div>
    </section>
  );
}

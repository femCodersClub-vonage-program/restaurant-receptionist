import Link from "next/link";
import { ArrowRight } from "lucide-react";

import { Header } from "@/components/ui";

export default function HomePage() {
  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-6 p-6">
      <Header
        title="Restaurant Receptionist"
        description="A reservation journey demo for a fictional restaurant, from incoming call to staff dashboard."
        actions={
          <Link
            href="/dashboard"
            className="inline-flex items-center justify-center gap-2 rounded-control bg-primary px-4 py-2 text-body text-white hover:bg-primary/90"
          >
            Open staff dashboard
            <ArrowRight className="size-4" aria-hidden="true" />
          </Link>
        }
      />
    </main>
  );
}

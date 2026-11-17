import { Header, Input } from "@/components/ui";
import { ServiceSection } from "@/components/dashboard";

export default function DashboardPage() {
  return (
    <main className="mx-auto flex w-full max-w-4xl flex-col gap-8 p-6">
      <Header
        title="Staff dashboard"
        description="Review lunch and dinner reservations and capacity for the selected date."
      />

      <div className="flex w-full max-w-sm flex-col gap-2">
        <label htmlFor="selected-date" className="text-label">
          Date
        </label>
        <Input
          id="selected-date"
          name="selectedDate"
          type="date"
          aria-describedby="selected-date-help"
        />
        <p id="selected-date-help" className="text-label text-foreground-muted">
          Reservation and capacity data for the selected date will appear here
          once it is connected.
        </p>
      </div>

      <div className="flex flex-col gap-6">
        <ServiceSection
          title="Lunch"
          arrivalWindow="13:00–16:00"
          total={30}
          occupied={0}
          available={30}
          status="empty"
        />
        <ServiceSection
          title="Dinner"
          arrivalWindow="20:00–23:30"
          total={30}
          occupied={0}
          available={30}
          status="empty"
        />
      </div>
    </main>
  );
}

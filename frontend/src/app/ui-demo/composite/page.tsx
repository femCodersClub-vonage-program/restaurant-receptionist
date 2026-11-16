"use client";

import {
  Button,
  CapacityIndicator,
  FormField,
  Header,
  Input,
  Select,
  Table,
} from "@/components/ui";
import type { TableColumn } from "@/components/ui";

type DemoReservation = {
  id: string;
  name: string;
  time: string;
  party: string;
};

const reservationColumns: TableColumn<DemoReservation>[] = [
  { key: "name", header: "Name", cell: (row) => row.name },
  { key: "time", header: "Time", cell: (row) => row.time },
  { key: "party", header: "Party", cell: (row) => row.party },
];

const reservations: DemoReservation[] = [
  { id: "1", name: "Jane Doe", time: "13:30", party: "4 guests" },
  { id: "2", name: "Alex Smith", time: "14:00", party: "2 guests" },
];

export default function CompositeDemoPage() {
  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-8 p-6">
      <Header
        title="Composite UI components"
        description="A demonstration of the shared composite components and their states."
        actions={
          <>
            <Button variant="secondary">Refresh</Button>
            <Button>New reservation</Button>
          </>
        }
      />

      <section className="flex flex-col gap-4">
        <h2 className="text-heading-md">FormField</h2>
        <div className="flex flex-col gap-6 rounded-surface border border-border bg-surface p-6">
          <FormField
            label="Customer name"
            required
            helpText="Name used for the reservation."
          >
            {(props) => <Input {...props} placeholder="Jane Doe" />}
          </FormField>
          <FormField
            label="Service"
            required
            helpText="Lunch or dinner service."
          >
            {(props) => (
              <Select {...props} defaultValue="">
                <option value="" disabled>
                  Select a service
                </option>
                <option value="lunch">Lunch</option>
                <option value="dinner">Dinner</option>
              </Select>
            )}
          </FormField>
          <FormField
            label="Phone number"
            required
            error="Enter a valid phone number."
          >
            {(props) => <Input {...props} placeholder="612345678" />}
          </FormField>
        </div>
      </section>

      <section className="flex flex-col gap-4">
        <h2 className="text-heading-md">Table</h2>
        <div className="rounded-surface border border-border bg-surface p-6">
          <Table
            caption="Lunch reservations"
            columns={reservationColumns}
            rows={reservations}
            rowKey={(row) => row.id}
            emptyMessage="No reservations for this service yet."
          />
        </div>
        <div className="rounded-surface border border-border bg-surface p-6">
          <Table
            caption="Dinner reservations"
            columns={reservationColumns}
            rows={[]}
            rowKey={(row) => row.id}
            emptyMessage="No reservations for this service yet."
          />
        </div>
      </section>

      <section className="flex flex-col gap-4">
        <h2 className="text-heading-md">CapacityIndicator</h2>
        <div className="flex flex-col gap-6 rounded-surface border border-border bg-surface p-6">
          <CapacityIndicator
            label="Lunch"
            total={30}
            occupied={12}
            available={18}
          />
          <CapacityIndicator
            label="Dinner"
            total={30}
            occupied={30}
            available={0}
          />
        </div>
      </section>

      <p>
        <a href="/ui-demo" className="text-primary underline">
          Back to base UI components
        </a>
      </p>
    </main>
  );
}

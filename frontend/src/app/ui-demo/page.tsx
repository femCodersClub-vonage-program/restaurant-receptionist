"use client";

import { useState } from "react";

import { Alert, Badge, Button, Card, Input, Select } from "@/components/ui";

export default function UiDemoPage() {
  const [saving, setSaving] = useState(false);

  function handleSave() {
    setSaving(true);
    window.setTimeout(() => setSaving(false), 1500);
  }

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-8 p-6">
      <div className="flex flex-col gap-2">
        <h1 className="text-heading-lg">Base UI components</h1>
        <p className="text-foreground-muted">
          A demonstration of the shared base components and their states.
        </p>
      </div>

      <section className="flex flex-col gap-4">
        <h2 className="text-heading-md">Button</h2>
        <div className="flex flex-wrap items-center gap-3">
          <Button>Primary</Button>
          <Button variant="secondary">Secondary</Button>
          <Button disabled>Disabled</Button>
          <Button variant="secondary" disabled>
            Secondary disabled
          </Button>
          <Button onClick={handleSave} loading={saving}>
            {saving ? "Saving..." : "Save"}
          </Button>
        </div>
      </section>

      <section className="flex flex-col gap-4">
        <h2 className="text-heading-md">Input</h2>
        <div className="flex w-full max-w-sm flex-col gap-2">
          <label htmlFor="customer-name" className="text-label">
            Customer name
          </label>
          <Input id="customer-name" name="customerName" placeholder="Jane Doe" />
        </div>
        <div className="flex w-full max-w-sm flex-col gap-2">
          <label htmlFor="notes-disabled" className="text-label">
            Disabled input
          </label>
          <Input id="notes-disabled" disabled placeholder="Unavailable" />
        </div>
        <div className="flex w-full max-w-sm flex-col gap-2">
          <label htmlFor="phone-error" className="text-label">
            Phone number
          </label>
          <Input
            id="phone-error"
            error
            aria-describedby="phone-error-message"
            placeholder="612345678"
          />
          <p id="phone-error-message" className="text-label text-error">
            Enter a valid phone number.
          </p>
        </div>
      </section>

      <section className="flex flex-col gap-4">
        <h2 className="text-heading-md">Select</h2>
        <div className="flex w-full max-w-sm flex-col gap-2">
          <label htmlFor="service" className="text-label">
            Service
          </label>
          <Select id="service" name="service" defaultValue="lunch">
            <option value="lunch">Lunch</option>
            <option value="dinner">Dinner</option>
          </Select>
        </div>
        <div className="flex w-full max-w-sm flex-col gap-2">
          <label htmlFor="party-size" className="text-label">
            Party size
          </label>
          <Select id="party-size" name="partySize" disabled defaultValue="2">
            <option value="2">2 guests</option>
          </Select>
        </div>
        <div className="flex w-full max-w-sm flex-col gap-2">
          <label htmlFor="service-error" className="text-label">
            Service
          </label>
          <Select
            id="service-error"
            name="serviceError"
            error
            aria-describedby="service-error-message"
            defaultValue=""
          >
            <option value="" disabled>
              Select a service
            </option>
            <option value="lunch">Lunch</option>
            <option value="dinner">Dinner</option>
          </Select>
          <p id="service-error-message" className="text-label text-error">
            Select a service to continue.
          </p>
        </div>
      </section>

      <section className="flex flex-col gap-4">
        <h2 className="text-heading-md">Badge</h2>
        <div className="flex flex-wrap items-center gap-3">
          <Badge>Neutral</Badge>
          <Badge variant="success">Confirmed</Badge>
          <Badge variant="warning">Pending</Badge>
          <Badge variant="error">Failed</Badge>
        </div>
      </section>

      <section className="flex flex-col gap-4">
        <h2 className="text-heading-md">Card</h2>
        <Card className="p-6">
          <div className="flex flex-col gap-2">
            <h3 className="text-heading-sm">Reservation summary</h3>
            <p className="text-foreground-muted">
              A card is a bounded surface for related content.
            </p>
          </div>
        </Card>
      </section>

      <section className="flex flex-col gap-4">
        <h2 className="text-heading-md">Alert</h2>
        <div className="flex flex-col gap-3">
          <Alert variant="success" title="Reservation confirmed">
            The reservation was saved and a confirmation message was requested.
          </Alert>
          <Alert variant="warning" title="Service nearly full">
            Only a few places remain for this service.
          </Alert>
          <Alert variant="error" title="Could not save reservation">
            The requested party exceeds the available capacity.
          </Alert>
        </div>
      </section>
    </main>
  );
}

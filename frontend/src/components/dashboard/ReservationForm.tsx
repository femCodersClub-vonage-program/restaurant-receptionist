"use client";

import { useEffect, useId, useRef, useState } from "react";
import type { ChangeEvent, FormEvent } from "react";

import { Button, Card, FormField, Input } from "@/components/ui";

export interface ReservationFormProps {
  defaultDate: string;
  arrivalWindowsHint: string;
  onClose: () => void;
}

interface ReservationFormValues {
  customerFirstName: string;
  customerLastName: string;
  phoneNumber: string;
  partySize: string;
  reservationDate: string;
  arrivalTime: string;
}

export function ReservationForm({
  defaultDate,
  arrivalWindowsHint,
  onClose,
}: ReservationFormProps) {
  const headingId = useId();
  const firstFieldRef = useRef<HTMLInputElement>(null);
  const [values, setValues] = useState<ReservationFormValues>({
    customerFirstName: "",
    customerLastName: "",
    phoneNumber: "",
    partySize: "",
    reservationDate: defaultDate,
    arrivalTime: "",
  });

  useEffect(() => {
    firstFieldRef.current?.focus();
  }, []);

  function handleChange(field: keyof ReservationFormValues) {
    return (event: ChangeEvent<HTMLInputElement>) => {
      const { value } = event.target;
      setValues((current) => ({ ...current, [field]: value }));
    };
  }

  // Submission is wired in a later step; the skeleton only captures input.
  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
  }

  return (
    <Card className="p-6">
      <form
        noValidate
        aria-labelledby={headingId}
        onSubmit={handleSubmit}
        className="flex flex-col gap-6"
      >
        <h2 id={headingId} className="text-heading-md">
          New reservation
        </h2>

        <div className="grid gap-4 sm:grid-cols-2">
          <FormField label="First name" required>
            {(controlProps) => (
              <Input
                {...controlProps}
                ref={firstFieldRef}
                name="customerFirstName"
                autoComplete="off"
                value={values.customerFirstName}
                onChange={handleChange("customerFirstName")}
              />
            )}
          </FormField>

          <FormField label="Last name" required>
            {(controlProps) => (
              <Input
                {...controlProps}
                name="customerLastName"
                autoComplete="off"
                value={values.customerLastName}
                onChange={handleChange("customerLastName")}
              />
            )}
          </FormField>

          <FormField
            label="Phone"
            required
            helpText="Spanish number, for example 600 000 000 or +34 600 000 000."
          >
            {(controlProps) => (
              <Input
                {...controlProps}
                name="phoneNumber"
                type="tel"
                inputMode="tel"
                autoComplete="off"
                value={values.phoneNumber}
                onChange={handleChange("phoneNumber")}
              />
            )}
          </FormField>

          <FormField label="Party size" required>
            {(controlProps) => (
              <Input
                {...controlProps}
                name="partySize"
                type="number"
                inputMode="numeric"
                step={1}
                value={values.partySize}
                onChange={handleChange("partySize")}
              />
            )}
          </FormField>

          <FormField label="Reservation date" required>
            {(controlProps) => (
              <Input
                {...controlProps}
                name="reservationDate"
                type="date"
                value={values.reservationDate}
                onChange={handleChange("reservationDate")}
              />
            )}
          </FormField>

          <FormField
            label="Arrival time"
            required
            helpText={`Arrival windows: ${arrivalWindowsHint}.`}
          >
            {(controlProps) => (
              <Input
                {...controlProps}
                name="arrivalTime"
                type="time"
                value={values.arrivalTime}
                onChange={handleChange("arrivalTime")}
              />
            )}
          </FormField>
        </div>

        <p className="text-label text-foreground-muted">
          Read the details back to the customer and get their confirmation
          before submitting.
        </p>

        <div className="flex flex-wrap gap-3">
          <Button type="submit" disabled>
            Create reservation
          </Button>
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
        </div>
      </form>
    </Card>
  );
}

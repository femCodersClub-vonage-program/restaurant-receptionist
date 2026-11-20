"use client";

import { useCallback, useEffect, useRef, useState } from "react";

import { Button, Header, Input } from "@/components/ui";
import { ServiceSection } from "@/components/dashboard";
import {
  fetchReservationsByDate,
  type DiningSessionKey,
  type ReservationsResponse,
} from "@/lib/reservations";

type DashboardStatus = "loading" | "success" | "error";

interface SessionDescriptor {
  key: DiningSessionKey;
  title: string;
  arrivalWindow: string;
}

const SESSIONS: SessionDescriptor[] = [
  { key: "LUNCH", title: "Lunch", arrivalWindow: "13:00–16:00" },
  { key: "DINNER", title: "Dinner", arrivalWindow: "20:00–23:30" },
];

const GENERIC_ERROR =
  "Reservation data could not be loaded. Please try again.";

function todayInEuropeMadrid(): string {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: "Europe/Madrid",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(new Date());

  const value = (type: string) =>
    parts.find((part) => part.type === type)?.value ?? "";

  return `${value("year")}-${value("month")}-${value("day")}`;
}

function formatReadableDate(date: string): string {
  const [year, month, day] = date.split("-").map(Number);
  const parsed = new Date(Date.UTC(year, month - 1, day));
  return new Intl.DateTimeFormat("en-GB", {
    timeZone: "UTC",
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(parsed);
}

export default function DashboardView() {
  const [date, setDate] = useState<string>(() => todayInEuropeMadrid());
  const [data, setData] = useState<ReservationsResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [announcement, setAnnouncement] = useState<string>("");
  const latestRequestRef = useRef(0);

  const status: DashboardStatus =
    data === null && errorMessage === null
      ? "loading"
      : errorMessage !== null
        ? "error"
        : "success";

  const loadForDate = useCallback((targetDate: string) => {
    const requestId = ++latestRequestRef.current;
    fetchReservationsByDate(targetDate)
      .then((result) => {
        if (latestRequestRef.current !== requestId) {
          return;
        }
        setData(result);
        setErrorMessage(null);
        setAnnouncement(`Loaded reservations for ${formatReadableDate(targetDate)}.`);
      })
      .catch((error: unknown) => {
        if (latestRequestRef.current !== requestId) {
          return;
        }
        setData(null);
        setErrorMessage(error instanceof Error ? error.message : GENERIC_ERROR);
        setAnnouncement("Could not load reservations. Please try again.");
      });
  }, []);

  useEffect(() => {
    loadForDate(date);
  }, [date, loadForDate]);

  function handleDateChange(nextDate: string) {
    if (!nextDate) {
      return;
    }
    setDate(nextDate);
    setData(null);
    setErrorMessage(null);
  }

  function handleRefresh() {
    setData(null);
    setErrorMessage(null);
    loadForDate(date);
  }

  return (
    <main className="mx-auto flex w-full max-w-4xl flex-col gap-8 p-6">
      <Header
        title="Staff dashboard"
        description="Review lunch and dinner reservations and capacity for the selected date."
      />

      <div aria-live="polite" className="sr-only">
        {announcement}
      </div>

      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:gap-3">
        <div className="flex w-full max-w-sm flex-col gap-2">
          <label htmlFor="selected-date" className="text-label">
            Date
          </label>
          <Input
            id="selected-date"
            name="selectedDate"
            type="date"
            value={date}
            onChange={(event) => handleDateChange(event.target.value)}
          />
        </div>
        <Button
          variant="secondary"
          loading={status === "loading"}
          onClick={handleRefresh}
        >
          Refresh
        </Button>
      </div>

      <div className="flex flex-col gap-6">
        {SESSIONS.map((session) => {
          const sessionData = data?.sessions[session.key];
          return (
            <ServiceSection
              key={session.key}
              title={session.title}
              arrivalWindow={session.arrivalWindow}
              total={sessionData?.capacity.totalCapacity ?? 0}
              occupied={sessionData?.capacity.occupiedCapacity ?? 0}
              available={sessionData?.capacity.availableCapacity ?? 0}
              reservations={sessionData?.reservations ?? []}
              status={status}
              error={errorMessage ?? undefined}
            />
          );
        })}
      </div>
    </main>
  );
}

import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "MediSlot | Book smarter, wait shorter",
  description: "Find healthcare providers and manage appointments with MediSlot.",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}

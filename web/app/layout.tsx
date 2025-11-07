import type { Metadata } from "next";
import "./globals.css";

/**
 * Root layout component for RapidPhotoUpload web application.
 * 
 * Provides the base HTML structure and global styles for all pages.
 */
export const metadata: Metadata = {
  title: "RapidPhotoUpload",
  description: "High-performance asynchronous photo upload system",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        {children}
      </body>
    </html>
  );
}

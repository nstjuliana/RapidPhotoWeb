import type { Metadata } from "next";
import "./globals.css";
import { Providers } from "@/components/providers";

/**
 * Root layout component for RapidPhotoUpload web application.
 * 
 * Provides the base HTML structure and global styles for all pages.
 * Wraps the application with TanStack Query provider.
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
      <body className="antialiased" suppressHydrationWarning>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}

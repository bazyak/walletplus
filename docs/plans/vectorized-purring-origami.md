# Plan: Add Supported Pass Formats Section to README

## Context

The README describes features and architecture but has no consolidated reference showing which pass formats the app supports and how they compare. A formats table gives users and contributors a quick overview of capabilities per format.

## What to Add

Insert a new **Supported Pass Formats** section into `README.md` between the existing **Features** section and **Getting Started**. A single comparison table covers the two implemented formats (PKPass and Custom) plus Google Wallet as a planned entry.

## Table Design

| Format | Source | Barcode types | Images | Localization | Background refresh | Pass categories |
|---|---|---|---|---|---|---|
| **PKPass** (Apple Wallet) | `.pkpass` file or QR scan | QR, PDF417, Aztec, Code128 | Yes (icon, logo, strip, background; @1x–@3x) | Yes (`.lproj` strings) | Yes (webServiceURL) | Boarding pass, Event ticket, Coupon, Store card, Generic |
| **Custom** | Camera scan / manual entry | QR, Code 128, Code 39, Code 93, Codabar, Data Matrix, EAN-13, EAN-8, ITF, UPC-A, UPC-E, PDF417, Aztec | No | No | No | Loyalty card |
| **Google Wallet** | — | — | — | — | — | Planned |

## File to Modify

- `README.md` — insert new section after the Features bullet list, before the Getting Started section

## Verification

Open the rendered README in GitHub (or any Markdown previewer) and confirm:
- The section heading appears between Features and Getting Started
- Table renders correctly with all columns aligned
- Google Wallet row clearly marked as Planned

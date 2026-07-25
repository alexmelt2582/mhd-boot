/**
 * Converts a hex color string to an RGB string format "r g b"
 * suitable for Tailwind CSS variable usage with opacity modifiers.
 * @param hex - Hex color string (e.g., "#ec4899")
 * @returns String "r g b" (e.g., "236 72 153")
 */
export const hexToRgbChannels = (hex: string): string => {
  let c: any;
  if (/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)) {
    c = hex.substring(1).split('');
    if (c.length === 3) {
      c = [c[0], c[0], c[1], c[1], c[2], c[2]];
    }
    c = '0x' + c.join('');
    return `${(c >> 16) & 255} ${(c >> 8) & 255} ${c & 255}`;
  }
  // Default fallback
  return '236 72 153';
};

export interface ThemeColor {
  name: string;
  hex: string;
}

export const PRESET_COLORS: ThemeColor[] = [
  { name: 'Muse Pink', hex: '#ec4899' },
  { name: 'Electric Violet', hex: '#8b5cf6' },
  { name: 'Ocean Blue', hex: '#3b82f6' },
  { name: 'Emerald Green', hex: '#10b981' },
  { name: 'Sunset Orange', hex: '#f97316' },
  { name: 'Crimson Red', hex: '#ef4444' },
];

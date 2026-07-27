module.exports = {
  mode: "jit",
  purge: ["./public/**/*.html", "./src/**/*.{js,jsx,ts,tsx,vue}"],
  corePlugins: {
    preflight: false
  },
  darkMode: false, // or 'media' or 'class'
  theme: {
    extend: {
      colors: {
        meTheme: "var(--me-theme-color)",
        meBackground: "var(--me-background-color)",
        meHoverBackground: "var(--me-hover-background-color)",
        meActiveBackground: "var(--me-active-background-color)",
        meText: "var(--me-text-color)",
        meActiveText: "var(--me-active-text-color)",
        meHoverText: "var(--me-hover-text-color)",
        meHintText: "var(--me-hint-text-color)",
        meSuccess: "var(--me-success-color)",
        meWarning: "var(--me-warning-color)",
        meDanger: "var(--me-danger-color)"
      }
    }
  },
  variants: {
    extend: {}
  },
  plugins: [require("@tailwindcss/line-clamp")]
};

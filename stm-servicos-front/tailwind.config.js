/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  theme: {
    extend: {
      colors: {
        brand: {
          darkest: '#1A2332',
          dark:    '#1E3A5F',
          base:    '#2A5FA0',
          light:   '#3A7CC4',
          bg:      '#EAF3FC',
        },
        accent: {
          DEFAULT: '#F97316',
          hover:   '#EA6010',
          bg:      '#FFF3E6',
        },
      },
    },
  },
  plugins: [],
}
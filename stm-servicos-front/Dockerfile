# ===============================
# Stage 1 - Build
# ===============================
FROM node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install

COPY . .

RUN npm run build -- --configuration production --project=stm-servicos-front

# ===============================
# Stage 2 - NGINX
# ===============================
FROM nginx:alpine

# Remove config padrão
RUN rm /etc/nginx/conf.d/default.conf

# Copia config SPA
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Remove conteúdo padrão
RUN rm -rf /usr/share/nginx/html/*

# Copia build Angular
COPY --from=build /app/dist/stm-servicos-front/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

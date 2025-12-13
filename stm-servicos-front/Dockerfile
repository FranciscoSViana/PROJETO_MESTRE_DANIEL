# ===============================
# Stage 1 - Build
# ===============================
FROM node:22-alpine AS build

WORKDIR /app
COPY ./package.json ./package-lock.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration production --project=stm-servicos-front

# ===============================
# Stage 2 - NGINX
# ===============================
FROM nginx:alpine

# Remove config padrão
RUN rm /etc/nginx/conf.d/default.conf

# Copia template
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Copia build Angular
COPY --from=build /app/dist/stm-servicos-front/browser /usr/share/nginx/html

# Cloud Run
EXPOSE 8080

# 🔥 CONVERTE ${PORT} EM TEMPO DE EXECUÇÃO
CMD /bin/sh -c "envsubst '\$PORT' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"

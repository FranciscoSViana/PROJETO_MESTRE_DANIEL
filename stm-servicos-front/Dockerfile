# ===============================
# Stage 1 - Build
# ===============================
FROM node:22-alpine AS build

WORKDIR /app

COPY ./package.json ./package-lock.json ./

RUN npm install

COPY . .

# Angular 19 - build de produção
RUN npm run build -- --configuration production --project=stm-servicos-front

# ===============================
# Stage 2 - NGINX
# ===============================
FROM nginx:alpine

# Remove config padrão
RUN rm /etc/nginx/conf.d/default.conf

# Copia config customizada (porta 8080)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia os arquivos buildados
COPY --from=build /app/dist/stm-servicos-front/browser /usr/share/nginx/html

# Porta exigida pelo Cloud Run
EXPOSE 8080

# ✅ FORMA CORRETA de iniciar nginx
CMD ["nginx", "-g", "daemon off;"]
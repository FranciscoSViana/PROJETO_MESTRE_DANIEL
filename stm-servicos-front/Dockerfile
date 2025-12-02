FROM node:22-alpine AS build

WORKDIR /app

COPY ./package.json ./package-lock.json ./

RUN npm install

COPY . .

RUN npm run build --prod

FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

COPY --from=build /app/dist/stm-servicos-front/browser /usr/share/nginx/html

EXPOSE 80

ENTRYPOINT [ "nginx -g daemon off" ]
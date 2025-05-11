FROM node:20-alpine AS build-stage

WORKDIR /app

COPY ../../whatsapp-clone-ui/package*.json ./

RUN npm install

COPY ../../whatsapp-clone-ui .

RUN npm run build --prod

FROM nginx:alpine

COPY ../../whatsapp-clone-ui/nginx.conf /etc/nginx/nginx.conf

COPY --from=build-stage /app/dist/whatsapp-clone-ui /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]



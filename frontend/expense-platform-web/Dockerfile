# Stage 1: build the static bundle
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

COPY . .
RUN npm run build

# Stage 2: serve with nginx
FROM nginx:alpine

# Custom nginx config (SPA fallback so client-side routes don't 404)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the built static files
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
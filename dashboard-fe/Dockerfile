FROM 10.113.16.240/node:20-alpine AS builder

WORKDIR /app

# Copy package files first for better caching
COPY package.json package-lock.json* ./

# Install dependencies
RUN npm install

# Copy source
COPY . .

# Build
RUN npm run build

# Production image with nginx
FROM 10.113.16.240/nginx:alpine

ENV TZ=Asia/Shanghai
ENV BACKEND_HOST=backend
ENV BACKEND_PORT=8080

# Copy built assets
COPY --from=builder /app/dist /usr/share/nginx/html

# Nginx config template
COPY nginx.conf.template /etc/nginx/conf.d/default.conf.template

# Startup script
COPY start-nginx.sh /docker-entrypoint.d/start-nginx.sh
RUN chmod +x /docker-entrypoint.d/start-nginx.sh

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

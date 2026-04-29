DEV  = docker-compose -p glance-dev  -f docker-compose.dev.yml  --env-file .env.dev
PROD = docker-compose -p glance-prod -f docker-compose.prod.yml --env-file .env.prod

dev:
	$(DEV) up -d

down-dev:
	$(DEV) down

build-dev:
	$(DEV) down && $(DEV) up -d --build

prod:
	$(PROD) up -d

down-prod:
	$(PROD) down

build-prod:
	$(PROD) down && $(PROD) up -d --build

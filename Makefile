.PHONY: generate

PROTO_SRC_DIR=./src/main/java/dk/raijin/jafka/protos
PROTO_OUT_DIR=./src/main/java

generate:
	@echo "Generating proto files"
	protoc -I=$(PROTO_SRC_DIR) --java_out=$(PROTO_OUT_DIR) $(PROTO_SRC_DIR)/*.proto
	@echo "Generated proto files"
up:
	@echo "Spinning up containers"
	docker compose up --build -d
	@echo "Containers are up... maybe :thinking:"

down:
	@echo "Spinning down containers"
	docker compose down
	@echo "Containers are down... maybe :thinking:"
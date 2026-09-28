CXX      = g++
CXXFLAGS = -Wall -std=c++11 -Iinclude -Igenerated $(shell pkg-config --cflags omniORB4 2>/dev/null)
LDFLAGS  = $(shell pkg-config --libs omniORB4 2>/dev/null) -lomnithread -lpthread
MYSQL_CFLAGS = $(shell mysql_config --cflags)
MYSQL_LIBS   = $(shell mysql_config --libs)

IDL      = ../idl/AppServices.idl
GEN_DIR  = generated
GEN_SRC  = $(GEN_DIR)/AppServicesSK.cpp
GEN_HDR  = $(GEN_DIR)/AppServices.h

.PHONY: all clean

all: dbserver client

# Regenere les souches C++ a partir de l'IDL des que celui-ci change
$(GEN_SRC) $(GEN_HDR): $(IDL)
	mkdir -p $(GEN_DIR)
	omniidl -bcxx -Wba -Wbtp -Wbh=.h -Wbs=SK.cpp -C $(GEN_DIR) $(IDL)

dbserver: src/DBServer.cpp src/DBServiceImpl.cpp $(GEN_SRC) $(GEN_HDR)
	$(CXX) $(CXXFLAGS) $(MYSQL_CFLAGS) -o $@ src/DBServer.cpp src/DBServiceImpl.cpp $(GEN_SRC) $(LDFLAGS) $(MYSQL_LIBS)

client: src/Client.cpp $(GEN_SRC) $(GEN_HDR)
	$(CXX) $(CXXFLAGS) -o $@ src/Client.cpp $(GEN_SRC) $(LDFLAGS)

clean:
	rm -f dbserver client
	rm -rf $(GEN_DIR)

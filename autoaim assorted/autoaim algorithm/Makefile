CXX = g++
CXXFLAGS = -Wall -Wextra -std=c++17 -O2

# The path to the generated Rust static library
LIBS = target/release/libautoaim.a

all: run

target/release/libautoaim.a: src/lib.rs
	cargo build --release

autoaim_test: cpp/main.cpp target/release/libautoaim.a
	$(CXX) $(CXXFLAGS) -o autoaim_test cpp/main.cpp $(LIBS) -lpthread -ldl -lm

run: autoaim_test
	./autoaim_test

clean:
	cargo clean
	rm -f autoaim_test

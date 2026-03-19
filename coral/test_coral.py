#!/usr/bin/env python3
import os
import urllib.request
import platform

print("========================================")
print(" Google Coral Edge TPU Hardware Sandbox")
print("========================================")

try:
    import tflite_runtime.interpreter as tflite
except ImportError:
    print(">> Warning: tflite_runtime not natively installed.")
    print(">> Attempting fallback to full tensorflow API...")
    try:
        import tensorflow.lite as tflite
    except ImportError:
        print("\n[!] FATAL: Neither tflite_runtime nor tensorflow are installed.")
        print("    Google hosts the TPU library externally. Run:")
        print("    pip install --extra-index-url https://google-coral.github.io/py-repo/ tflite_runtime")
        exit(1)

model_file = 'mobilenet_v2_1.0_224_quant_edgetpu.tflite'
if not os.path.exists(model_file):
    print(f">> Downloading {model_file} directly from Google servers...")
    url = f'https://github.com/google-coral/edgetpu/raw/master/test_data/{model_file}'
    urllib.request.urlretrieve(url, model_file)

try:
    print(">> Invoking libedgetpu hardware delegate...")
    
    # Platform specific delegate paths just in case
    delegate_path = 'libedgetpu.so.1'
    if platform.system() == 'Darwin':
        delegate_path = 'libedgetpu.1.dylib'
    elif platform.system() == 'Windows':
        delegate_path = 'edgetpu.dll'

    delegate = tflite.load_delegate(delegate_path)
    
    print(">> Injecting quantized model into TPU accelerator...")
    interpreter = tflite.Interpreter(
        model_path=model_file,
        experimental_delegates=[delegate]
    )
    
    print(">> Allocating memory tensors mapping...")
    interpreter.allocate_tensors()
    
    print("\n[SUCCESS] -> Hardware lock achieved!")
    print("          -> The Google Coral Edge TPU is fully responding and ready for FRC Neural Net inferencing!")
    
except ValueError as e:
    print(f"\n[ERROR] Hardware Exception: {e}")
    print(">> The Coral is either unplugged, or libedgetpu is missing from the system path.")
except Exception as e:
    print(f"\n[ERROR] Unknown Exception: {e}")

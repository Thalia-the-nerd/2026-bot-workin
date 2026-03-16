import os
import argparse
from datetime import datetime

# Configuration
LANG_MAP = {
    'h': 'C++ Headers',
    'hpp': 'C++ Headers',
    'inc': 'C++ Headers',
    'java': 'Java',
    'rs': 'Rust',
    'cpp': 'C++ Source',
    'md': 'Markdown',
    'json': 'JSON',
    'py': 'Python',
    'gradle': 'Gradle',
    'lock': 'Rust Lockfile',
    'toml': 'TOML',
    'yml': 'YAML',
    'sh': 'Shell',
}

EXCLUDE_DIRS = {'.git', '.github', '.vscode', '.wpilib', 'build', 'bin', 'node_modules'}

def analyze_repo(root_dir):
    stats = {}
    for root, dirs, files in os.walk(root_dir):
        # Skip excluded directories
        dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]
        
        for file in files:
            ext = file.split('.')[-1].lower() if '.' in file else 'no-ext'
            lang = LANG_MAP.get(ext, 'Other') if ext != 'no-ext' else 'Other'
            
            if lang not in stats:
                stats[lang] = {'files': 0, 'lines': 0, 'exts': set()}
            
            file_path = os.path.join(root, file)
            stats[lang]['files'] += 1
            if ext != 'no-ext':
                stats[lang]['exts'].add(f'.{ext}')
            
            # Count lines
            try:
                with open(file_path, 'rb') as f:
                    stats[lang]['lines'] += sum(1 for _ in f)
            except Exception:
                pass # Skip binary or inaccessible files

    return stats

def generate_markdown(stats):
    # Sort by line count for the pie chart
    sorted_stats = sorted(stats.items(), key=lambda x: x[1]['lines'], reverse=True)
    
    # Pie chart data
    mermaid_pie = "```mermaid\npie title Language Distribution (by Line Count)\n"
    for lang, data in sorted_stats:
        if data['lines'] > 0:
            mermaid_pie += f'    "{lang}" : {data["lines"]}\n'
    mermaid_pie += "```"

    # Table data
    table = "| Language | Extensions | File Count | Line Count |\n| :--- | :--- | :--- | :--- |\n"
    for lang, data in sorted_stats:
        exts = ", ".join(sorted(list(data['exts'])))
        table += f"| **{lang}** | `{exts}` | {data['files']:,} | {data['lines']:,} |\n"

    now = datetime.now().strftime("%Y-%m-%d")
    
    content = f"""# 📊 Repository Language Breakdown

A comprehensive analysis of the languages that power this codebase.

## 🧱 Codebase Composition

{mermaid_pie}

## 📈 Detailed Metrics

{table}

> [!NOTE]
> This breakdown is automatically generated. The heavy count of `.h` files is often due to external libraries included in the repository.

---
*Last updated on {now}*
"""
    return content

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', default='file_breakdown.md')
    args = parser.parse_args()
    
    stats = analyze_repo('.')
    markdown = generate_markdown(stats)
    
    with open(args.output, 'w') as f:
        f.write(markdown)
    
    print(f"Successfully generated {args.output}")

if __name__ == "__main__":
    main()

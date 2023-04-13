import argparse
import multiprocessing
import json
import re
import subprocess
from pathlib import Path
from typing import List

def update_overview() -> None:
    scores_by_solver = {}
    results_root = Path(__file__).parent / "results"

    for directory in results_root.iterdir():
        scores_by_seed = {}

        for file in directory.iterdir():
            if file.name.endswith(".txt"):
                scores_by_seed[file.stem] = int(file.read_text(encoding="utf-8").strip())

        scores_by_solver[directory.name] = scores_by_seed

    overview_template_file = Path(__file__).parent / "overview.tmpl.html"
    overview_file = Path(__file__).parent / "overview.html"

    overview_template = overview_template_file.read_text(encoding="utf-8")
    overview = overview_template.replace("/* scores_by_solver*/{}", json.dumps(scores_by_solver))

    with overview_file.open("w+", encoding="utf-8") as file:
        file.write(overview)

    print(f"Overview: file://{overview_file}")

def run(solver: Path, seeds: List[int], results_directory: Path) -> None:
    args = [
        "java",
        "-jar", str(Path(__file__).parent / "tester.jar"),
        "-exec", f"/home/jasper/.sdkman/candidates/java/8.0.345-tem/bin/java -jar {str(solver)}",
        "-seed", "{" + ",".join(map(str, seeds)) + "}",
        "-threads", str(min(len(seeds), multiprocessing.cpu_count() - 2)),
        "-novis",
        "-saveSolInput",
        "-saveSolOutput",
        "-saveSolError"
    ]

    if not results_directory.is_dir():
        results_directory.mkdir(parents=True)

    process = subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, cwd=results_directory)

    if process.returncode != 0:
        print(process.stdout)
        raise RuntimeError(f"Tester exited with status code {process.returncode}")

    output = process.stdout.decode("utf-8")

    for seed in seeds:
        pattern_prefix = f"Seed = {seed}, " if len(seeds) > 1 else ""
        score = re.search(pattern_prefix + r"Score = (-?\d+)\.0", output).group(1)

        print(f"{seed}: {score}")

        score_file = results_directory / f"{seed}.txt"
        with score_file.open("w+", encoding="utf-8") as file:
            file.write(score)

def main() -> None:
    parser = argparse.ArgumentParser(description="Run a solver.")
    parser.add_argument("solver", type=str, help="the solver to run")
    parser.add_argument("--seed", type=int, help="the seed to run (defaults to 1-100)")

    args = parser.parse_args()

    solver = Path(__file__).parent.parent / "build" / "libs" / f"{args.solver}.jar"
    if not solver.is_file():
        raise RuntimeError(f"Solver not found, {solver} is not a file")

    results_directory = Path(__file__).parent / "results" / args.solver

    if args.seed is None:
        run(solver, list(range(1, 101)), results_directory)
    else:
        run(solver, [args.seed], results_directory)

    update_overview()

if __name__ == "__main__":
    main()

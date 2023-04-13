import hcr
import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).parent.parent

solvers = sorted([file.stem for file in (PROJECT_ROOT / "build" / "libs").iterdir()])
default_seeds = list(range(1, 101))
is_maximizing = True
results_directory = PROJECT_ROOT / "tester" / "results"

def run_solvers(solver: str, inputs: list[int]) -> list[hcr.Output]:
    solver_jar = PROJECT_ROOT / "build" / "libs" / f"{solver}.jar"

    with hcr.temporary_directory() as output_directory:
        process = hcr.run_process([
            "java",
            "-jar", PROJECT_ROOT / "tester" / "tester.jar",
            "-exec", f"/home/jasper/.sdkman/candidates/java/8.0.345-tem/bin/java -jar {solver_jar}",
            "-seed", "{" + ",".join(map(str, inputs)) + "}",
            "-threads", hcr.thread_count(len(inputs)),
            "-novis",
            "-saveSolOutput",
            "-saveSolError",
        ], cwd=output_directory)

        outputs = []
        for input in inputs:
            stdout = (output_directory / f"{input}.out").read_text(encoding="utf-8")
            stderr = (output_directory / f"{input}.err").read_text(encoding="utf-8")

            pattern_prefix = f"Seed = {input}, " if len(inputs) > 1 else ""
            score = re.search(pattern_prefix + r"Score = (-?\d+)\.0", process.stdout).group(1)
            stderr += f"Score = {score}\n"

            outputs.append(hcr.Output(stdout, stderr))

        return outputs

def get_score(output: hcr.Output) -> float:
    return float(output.stderr.splitlines()[-1].split(" = ")[1])

if __name__ == "__main__":
    hcr.cli(solvers, default_seeds, is_maximizing, results_directory, get_score, run_solvers=run_solvers)

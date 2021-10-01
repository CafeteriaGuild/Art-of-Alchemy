package dev.cafeteria.artofalchemy.render.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import dev.cafeteria.artofalchemy.blockentity.BlockEntityPipe;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import net.minecraft.world.BlockRenderView;

public class ModelPipe implements UnbakedModel, BakedModel, FabricBakedModel {
	private static final class FaceMeshes {
		private Mesh tube;
		private Mesh blocker;
		private Mesh passivePort;
		private Mesh inserterPort;
		private Mesh extractorPort;
	}

	private static final class RingMeshEmitter {
		private final Matrix4f transformation;
		private final int left;
		private final int bottom;
		private final int right;
		private final int top;

		public RingMeshEmitter(
			final Matrix4f transformation, final int left, final int bottom, final int right, final int top
		) {
			this.transformation = transformation;
			this.left = left;
			this.bottom = bottom;
			this.right = right;
			this.top = top;
		}

		public void emit(final QuadEmitter emitter, final TexCoordEmitter texEmitter) {
			this.emitFront(emitter);
			texEmitter.emit(emitter);
			emitter.emit();

			this.emitLeft(emitter);
			texEmitter.emit(emitter);
			emitter.emit();

			this.emitBack(emitter);
			texEmitter.emit(emitter);
			emitter.emit();

			this.emitRight(emitter);
			texEmitter.emit(emitter);
			emitter.emit();
		}

		public void emitBack(final QuadEmitter emitter) {
			ModelPipe.emitPosSx(emitter, this.transformation, 0, this.right, this.bottom, this.right);
			ModelPipe.emitPosSx(emitter, this.transformation, 1, this.right, this.top, this.right);
			ModelPipe.emitPosSx(emitter, this.transformation, 2, this.left, this.top, this.right);
			ModelPipe.emitPosSx(emitter, this.transformation, 3, this.left, this.bottom, this.right);
		}

		public void emitFront(final QuadEmitter emitter) {
			ModelPipe.emitPosSx(emitter, this.transformation, 0, this.left, this.bottom, this.left);
			ModelPipe.emitPosSx(emitter, this.transformation, 1, this.left, this.top, this.left);
			ModelPipe.emitPosSx(emitter, this.transformation, 2, this.right, this.top, this.left);
			ModelPipe.emitPosSx(emitter, this.transformation, 3, this.right, this.bottom, this.left);
		}

		public void emitLeft(final QuadEmitter emitter) {
			ModelPipe.emitPosSx(emitter, this.transformation, 0, this.left, this.bottom, this.right);
			ModelPipe.emitPosSx(emitter, this.transformation, 1, this.left, this.top, this.right);
			ModelPipe.emitPosSx(emitter, this.transformation, 2, this.left, this.top, this.left);
			ModelPipe.emitPosSx(emitter, this.transformation, 3, this.left, this.bottom, this.left);
		}

		public void emitRight(final QuadEmitter emitter) {
			ModelPipe.emitPosSx(emitter, this.transformation, 0, this.right, this.bottom, this.left);
			ModelPipe.emitPosSx(emitter, this.transformation, 1, this.right, this.top, this.left);
			ModelPipe.emitPosSx(emitter, this.transformation, 2, this.right, this.top, this.right);
			ModelPipe.emitPosSx(emitter, this.transformation, 3, this.right, this.bottom, this.right);
		}
	}

	private static final class TexCoordEmitter {
		private final float minU;
		private final float maxU;
		private final float minV;
		private final float maxV;
		private int currentVertex = 0;

		// 'sx' stands for "sixteenth of a sprite texture area"
		public TexCoordEmitter(
			final Sprite sprite, final int sxMinU, final int sxMinV, final int sxMaxU, final int sxMaxV
		) {
			final float spriteMinU = sprite.getMinU();
			final float spriteMaxU = sprite.getMaxU();
			final float spriteMinV = sprite.getMinV();
			final float spriteMaxV = sprite.getMaxV();
			final float pxU = (spriteMaxU - spriteMinU) / 16.0f;
			final float pxV = (spriteMaxV - spriteMinV) / 16.0f;
			this.minU = spriteMinU + (pxU * sxMinU);
			this.maxU = spriteMinU + (pxU * sxMaxU);
			this.minV = spriteMinV + (pxV * sxMinV);
			this.maxV = spriteMinV + (pxV * sxMaxV);
		}

		public void emit(final QuadEmitter emitter) {
			// Start at origin (0, 0), and move in clockwise direction
			this.emitLowerLeft(emitter);
			this.emitUpperLeft(emitter);
			this.emitUpperRight(emitter);
			this.emitLowerRight(emitter);
			this.finishEmit(emitter);
		}

		public void emitLowerLeft(final QuadEmitter emitter) {
			emitter.sprite(this.nextVert(), 0, this.minU, this.minV);
		}

		public void emitLowerRight(final QuadEmitter emitter) {
			emitter.sprite(this.nextVert(), 0, this.maxU, this.minV);
		}

		public void emitUpperLeft(final QuadEmitter emitter) {
			emitter.sprite(this.nextVert(), 0, this.minU, this.maxV);
		}

		public void emitUpperRight(final QuadEmitter emitter) {
			emitter.sprite(this.nextVert(), 0, this.maxU, this.maxV);
		}

		public void finishEmit(final QuadEmitter emitter) {
			// Passing -1 as sprite color apparently activates texturing
			emitter.spriteColor(0, -1, -1, -1, -1);
		}

		private int nextVert() {
			final int cur = this.currentVertex;
			this.currentVertex = (this.currentVertex + 1) % 4;

			return cur;
		}
	}

	private static final int DIRECTION_COUNT = Direction.values().length;

	private static final SpriteIdentifier[] SPRITE_IDS = {
		new SpriteIdentifier(
			SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("artofalchemy:block/essentia_pipe_core")
		),
		new SpriteIdentifier(
			SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("artofalchemy:block/essentia_pipe_tube")
		),
		new SpriteIdentifier(
			SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("artofalchemy:block/essentia_pipe_blocker")
		),
		new SpriteIdentifier(
			SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("artofalchemy:block/essentia_pipe_sidecap")
		),
		new SpriteIdentifier(
			SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("artofalchemy:block/essentia_pipe_endcap")
		),
	};

	// Reuse scratch object to avoid allocations
	private static Vector4f scratchVector = new Vector4f();

	// Transformations that will rotate our base meshes into all 6 cardinal
	// directions
	private static Matrix4f[] buildCardinalTransformations() {
		final Matrix4f[] matrices = new Matrix4f[ModelPipe.DIRECTION_COUNT];

		final Matrix4f transform = new Matrix4f();
		transform.loadIdentity();
		matrices[Direction.DOWN.ordinal()] = new Matrix4f(transform);

		transform.multiply(Matrix4f.translate(0.5f, 0.5f, 0.5f));

		Matrix4f subTransform = new Matrix4f(transform);
		subTransform.multiply(new Quaternion(new Vec3f(0, 0, 1), 90, true));
		subTransform.multiply(Matrix4f.translate(-0.5f, -0.5f, -0.5f));
		matrices[Direction.EAST.ordinal()] = subTransform;

		subTransform = new Matrix4f(transform);
		subTransform.multiply(new Quaternion(new Vec3f(0, 0, 1), 180, true));
		subTransform.multiply(Matrix4f.translate(-0.5f, -0.5f, -0.5f));
		matrices[Direction.UP.ordinal()] = subTransform;

		subTransform = new Matrix4f(transform);
		subTransform.multiply(new Quaternion(new Vec3f(0, 0, 1), 270, true));
		subTransform.multiply(Matrix4f.translate(-0.5f, -0.5f, -0.5f));
		matrices[Direction.WEST.ordinal()] = subTransform;

		subTransform = new Matrix4f(transform);
		subTransform.multiply(new Quaternion(new Vec3f(1, 0, 0), 90, true));
		subTransform.multiply(Matrix4f.translate(-0.5f, -0.5f, -0.5f));
		matrices[Direction.NORTH.ordinal()] = subTransform;

		subTransform = new Matrix4f(transform);
		subTransform.multiply(new Quaternion(new Vec3f(1, 0, 0), 270, true));
		subTransform.multiply(Matrix4f.translate(-0.5f, -0.5f, -0.5f));
		matrices[Direction.SOUTH.ordinal()] = subTransform;

		return matrices;
	}

	private static void emitBlockerMesh(final QuadEmitter emitter, final Sprite sprite, final Matrix4f transformation) {
		// We have to build the ring here manually, since the texture for each face
		// differs
		TexCoordEmitter texEmitter;
		final RingMeshEmitter ringMeshEmitter = new RingMeshEmitter(transformation, 5, 4, 11, 5);

		ringMeshEmitter.emitFront(emitter);
		texEmitter = new TexCoordEmitter(sprite, 5, 5, 11, 6);
		texEmitter.emitUpperRight(emitter);
		texEmitter.emitLowerRight(emitter);
		texEmitter.emitLowerLeft(emitter);
		texEmitter.emitUpperLeft(emitter);
		texEmitter.finishEmit(emitter);
		emitter.emit();

		ringMeshEmitter.emitLeft(emitter);
		texEmitter = new TexCoordEmitter(sprite, 10, 5, 11, 11);
		texEmitter.emitUpperLeft(emitter);
		texEmitter.emitUpperRight(emitter);
		texEmitter.emitLowerRight(emitter);
		texEmitter.emitLowerLeft(emitter);
		texEmitter.finishEmit(emitter);
		emitter.emit();

		ringMeshEmitter.emitBack(emitter);
		new TexCoordEmitter(sprite, 5, 10, 11, 11).emit(emitter);
		emitter.emit();

		ringMeshEmitter.emitRight(emitter);
		texEmitter = new TexCoordEmitter(sprite, 5, 5, 6, 11);
		texEmitter.emitLowerRight(emitter);
		texEmitter.emitLowerLeft(emitter);
		texEmitter.emitUpperLeft(emitter);
		texEmitter.emitUpperRight(emitter);
		texEmitter.finishEmit(emitter);
		emitter.emit();

		ModelPipe.emitPosSx(emitter, transformation, 0, 11, 4, 5);
		ModelPipe.emitPosSx(emitter, transformation, 1, 11, 4, 11);
		ModelPipe.emitPosSx(emitter, transformation, 2, 5, 4, 11);
		ModelPipe.emitPosSx(emitter, transformation, 3, 5, 4, 5);
		new TexCoordEmitter(sprite, 5, 5, 11, 11).emit(emitter);
		emitter.emit();
	}

	private static void emitEndCapMesh(
		final QuadEmitter emitter, final TexCoordEmitter texEmitter, final Matrix4f transformation
	) {
		ModelPipe.emitPosSx(emitter, transformation, 0, 4, 4, 4);
		ModelPipe.emitPosSx(emitter, transformation, 1, 4, 4, 12);
		ModelPipe.emitPosSx(emitter, transformation, 2, 12, 4, 12);
		ModelPipe.emitPosSx(emitter, transformation, 3, 12, 4, 4);
		texEmitter.emit(emitter);
		emitter.emit();

		ModelPipe.emitPosSx(emitter, transformation, 0, 4, 0, 4);
		ModelPipe.emitPosSx(emitter, transformation, 1, 12, 0, 4);
		ModelPipe.emitPosSx(emitter, transformation, 2, 12, 0, 12);
		ModelPipe.emitPosSx(emitter, transformation, 3, 4, 0, 12);
		texEmitter.emit(emitter);
		emitter.emit();
	}

	private static void emitPortMesh(
		final QuadEmitter emitter, final TexCoordEmitter shortTubeTexEmitter, final TexCoordEmitter sideTexEmitter,
		final TexCoordEmitter endTexEmitter, final Matrix4f transformation
	) {
		// Short tube
		ModelPipe.emitTubeMesh(emitter, shortTubeTexEmitter, transformation, 1);
		// Side
		new RingMeshEmitter(transformation, 4, 0, 12, 4).emit(emitter, sideTexEmitter);
		// End
		ModelPipe.emitEndCapMesh(emitter, endTexEmitter, transformation);
	}

	private static void emitPosSx(
		final QuadEmitter emitter, final Matrix4f transformation, final int i, final int x, final int y, final int z
	) {
		ModelPipe.scratchVector.set(x / 16.0f, y / 16.0f, z / 16.0f, 1.0f);
		ModelPipe.scratchVector.transform(transformation);
		emitter.pos(i, ModelPipe.scratchVector.getX(), ModelPipe.scratchVector.getY(), ModelPipe.scratchVector.getZ());
	}

	private static void emitTubeMesh(
		final QuadEmitter emitter, final TexCoordEmitter texEmitter, final Matrix4f transformation, final int length
	) {
		final int l = 5 - length;
		new RingMeshEmitter(transformation, 6, l, 10, 5).emit(emitter, texEmitter);
	}

	private static void squareSx(
		final QuadEmitter emitter, final Direction nominalFace, final float left, final float bottom, final float right,
		final float top, final float depth
	) {
		emitter.square(nominalFace, left / 16.0f, bottom / 16.0f, right / 16.0f, top / 16.0f, depth / 16.0f);
	}

	private Sprite blockBreakSprite;

	private Mesh coreMesh;

	// Indexed via Direction
	private final FaceMeshes[] faceMeshes = new FaceMeshes[ModelPipe.DIRECTION_COUNT];

	@Override
	public BakedModel bake(
		final ModelLoader loader, final Function<SpriteIdentifier, Sprite> textureGetter,
		final ModelBakeSettings rotationContainer, final Identifier modelId
	) {
		final Sprite coreSprite = textureGetter.apply(ModelPipe.SPRITE_IDS[0]);
		final Sprite tubeSprite = textureGetter.apply(ModelPipe.SPRITE_IDS[1]);
		final Sprite blockerSprite = textureGetter.apply(ModelPipe.SPRITE_IDS[2]);
		final Sprite sideCapSprite = textureGetter.apply(ModelPipe.SPRITE_IDS[3]);
		final Sprite endCapSprite = textureGetter.apply(ModelPipe.SPRITE_IDS[4]);

		this.blockBreakSprite = coreSprite;

		final Renderer renderer = RendererAccess.INSTANCE.getRenderer();
		final MeshBuilder builder = renderer.meshBuilder();
		final QuadEmitter emitter = builder.getEmitter();

		// Build core mesh
		final TexCoordEmitter coreTexEmitter = new TexCoordEmitter(coreSprite, 5, 5, 11, 11);

		for (final Direction dir : Direction.values()) {
			ModelPipe.squareSx(emitter, dir, 5, 5, 11, 11, 5);
			coreTexEmitter.emit(emitter);
			emitter.emit();
		}

		this.coreMesh = builder.build();

		final Matrix4f[] cardTransforms = ModelPipe.buildCardinalTransformations();
		final TexCoordEmitter tubeTexEmitter = new TexCoordEmitter(tubeSprite, 0, 0, 4, 5);
		final TexCoordEmitter shortTubeTexEmitter = new TexCoordEmitter(tubeSprite, 0, 0, 4, 1);

		final TexCoordEmitter passivePortSideCapTexEmitter = new TexCoordEmitter(sideCapSprite, 0, 0, 8, 4);
		final TexCoordEmitter inserterPortSideCapTexEmitter = new TexCoordEmitter(sideCapSprite, 0, 12, 8, 16);
		final TexCoordEmitter extractorPortSideCapTexEmitter = new TexCoordEmitter(sideCapSprite, 0, 8, 8, 12);

		final TexCoordEmitter passivePortEndCapTexEmitter = new TexCoordEmitter(endCapSprite, 0, 0, 8, 8);
		final TexCoordEmitter inserterPortEndCapTexEmitter = new TexCoordEmitter(endCapSprite, 8, 8, 16, 16);
		final TexCoordEmitter extractorPortEndCapTexEmitter = new TexCoordEmitter(endCapSprite, 0, 8, 8, 16);

		for (int i = 0; i < ModelPipe.DIRECTION_COUNT; ++i) {
			final FaceMeshes meshes = new FaceMeshes();
			final Matrix4f transformation = cardTransforms[i];

			ModelPipe.emitTubeMesh(emitter, tubeTexEmitter, transformation, 5);
			meshes.tube = builder.build();

			ModelPipe.emitBlockerMesh(emitter, blockerSprite, transformation);
			meshes.blocker = builder.build();

			ModelPipe.emitPortMesh(
				emitter,
				shortTubeTexEmitter,
				passivePortSideCapTexEmitter,
				passivePortEndCapTexEmitter,
				transformation
			);
			meshes.passivePort = builder.build();

			ModelPipe.emitPortMesh(
				emitter,
				shortTubeTexEmitter,
				inserterPortSideCapTexEmitter,
				inserterPortEndCapTexEmitter,
				transformation
			);
			meshes.inserterPort = builder.build();

			ModelPipe.emitPortMesh(
				emitter,
				shortTubeTexEmitter,
				extractorPortSideCapTexEmitter,
				extractorPortEndCapTexEmitter,
				transformation
			);
			meshes.extractorPort = builder.build();

			this.faceMeshes[i] = meshes;
		}

		return this;
	}

	@Override
	public void emitBlockQuads(
		final BlockRenderView blockRenderView, final BlockState blockState, final BlockPos blockPos,
		final Supplier<Random> supplier, final RenderContext renderContext
	) {
		renderContext.meshConsumer().accept(this.coreMesh);

		final BlockEntityPipe.IOFace[] faceConfig = BlockEntityPipe.getRenderAttachedFaceConfig(blockRenderView, blockPos);
		for (int i = 0; i < ModelPipe.DIRECTION_COUNT; ++i) {
			final FaceMeshes meshes = this.faceMeshes[i];

			switch (faceConfig[i]) {
				case NONE:
					break;

				case CONNECT:
					renderContext.meshConsumer().accept(meshes.tube);
					break;

				case BLOCK:
					renderContext.meshConsumer().accept(meshes.blocker);
					break;

				case INSERTER:
					renderContext.meshConsumer().accept(meshes.inserterPort);
					break;

				case EXTRACTOR:
					renderContext.meshConsumer().accept(meshes.extractorPort);
					break;

				case PASSIVE:
					renderContext.meshConsumer().accept(meshes.passivePort);
					break;
			}
		}
	}

	@Override
	public void emitItemQuads(
		final ItemStack itemStack, final Supplier<Random> supplier, final RenderContext renderContext
	) {

	}

	// UnbakedModel
	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList(); // This model does not depend on other models.
	}

	@Override
	public ModelOverrideList getOverrides() {
		return null;
	}

	// BakedModel
	@Override
	public List<BakedQuad> getQuads(final BlockState state, final Direction face, final Random random) {
		// Fixme: Provide a sane fallback eventually, in case a mod oblivious to frapi
		// comes along.
		// Also see:
		// https://github.com/Cumulus-Mods/Art-of-Alchemy/pull/1#pullrequestreview-473530254
		return null;
	}

	@Override
	public Sprite getSprite() {
		return this.blockBreakSprite;
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(
		final Function<Identifier, UnbakedModel> unbakedModelGetter,
		final Set<Pair<String, String>> unresolvedTextureReferences
	) {
		return Arrays.asList(ModelPipe.SPRITE_IDS); // The textures this model (and all its model dependencies, and their
																								// dependencies, etc...!) depends on.
	}

	@Override
	public ModelTransformation getTransformation() {
		return null;
	}

	@Override
	public boolean hasDepth() {
		return false;
	}

	@Override
	public boolean isBuiltin() {
		return false;
	}

	@Override
	public boolean isSideLit() {
		return false;
	}

	// FabricBakedModel
	@Override
	public boolean isVanillaAdapter() {
		return false; // False to trigger FabricBakedModel rendering
	}

	@Override
	public boolean useAmbientOcclusion() {
		return false;
	}
}
